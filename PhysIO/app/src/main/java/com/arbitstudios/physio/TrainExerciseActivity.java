package com.arbitstudios.physio;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/* Reference taken from
 *  http://simena86.github.io/blog/2013/04/30/logging-accelerometer-from-android-to-pc/
 */

public class TrainExerciseActivity extends ActionBarActivity {
    // View elements
    TextView repsRemaining;
    ImageButton _startButton;
    ImageButton _keepButton;
    ImageButton _discardButton;
    // Media elements
    MediaPlayer countdownTrack;
    MediaPlayer winTrack;
    MediaPlayer failTrack;
    // Variables
    String _exerciseName;
    int _reps;
    int repCount;
    private double _linX, _linY, _linZ;
    private double _gyrX, _gyrY, _gyrZ;
    private double _oriX, _oriY, _oriZ;
    private double meanx=-1,meany=-1,meanz=-1;
    private double devx=-1,devy=-1,devz=-1;

    ArrayList<Double> magLinXList = new ArrayList<Double>(210);
    ArrayList<Double> magLinYList = new ArrayList<Double>(210);
    ArrayList<Double> magLinZList = new ArrayList<Double>(210);

    ArrayList<Double> linXList = new ArrayList<Double>(210);
    ArrayList<Double> linYList = new ArrayList<Double>(210);
    ArrayList<Double> linZList = new ArrayList<Double>(210);
    ArrayList<Double> gyrXList = new ArrayList<Double>(210);
    ArrayList<Double> gyrYList = new ArrayList<Double>(210);
    ArrayList<Double> gyrZList = new ArrayList<Double>(210);
    ArrayList<Double> oriXList = new ArrayList<Double>(210);
    ArrayList<Double> oriYList = new ArrayList<Double>(210);
    ArrayList<Double> oriZList = new ArrayList<Double>(210);

    // Sensor Variable
    private SensorManager _sensorManager;
    private Sensor _linearAccelerationSensor;
    private Sensor _gyroSensor;
    private Sensor _orientationSensor;
    boolean accStarted;

    // Sensor handler thread variable
    HandlerThread _handlerThread;
    Handler _handler;
    // File variables
    ObjectOutputStream _foutStream;
    private File _directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_exercise);

        Intent inputIntent = getIntent();
        _exerciseName = inputIntent.getStringExtra("ExerciseName");
        _reps = inputIntent.getIntExtra("Reps", 3);
        repCount = 0;
        setTitle(_exerciseName);
        createExerciseDirectory();

        // Initializing view elements
        repsRemaining = (TextView) findViewById(R.id.repsRemaining);
        _startButton = (ImageButton) findViewById(R.id.startTrainingButton);
        _keepButton = (ImageButton) findViewById(R.id.keepButton);
        _discardButton = (ImageButton) findViewById(R.id.discardButton);
        hide_keep_discard_buttons();

        // Initializing media files
        countdownTrack = MediaPlayer.create(getApplicationContext(), R.raw.countdown);
        winTrack = MediaPlayer.create(getApplicationContext(), R.raw.win);
        failTrack = MediaPlayer.create(getApplicationContext(), R.raw.fail);
        countdownTrack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        _startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countdownTrack.start();
                _startButton.setVisibility(View.INVISIBLE);
                //TODO: start animation
            }
        });
        _keepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    keep_or_discard(true);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        _discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    keep_or_discard(false);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        // Initialize the Sensors
        _sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        _linearAccelerationSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        _gyroSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        _orientationSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        upadteRepCount();

        _handlerThread = new HandlerThread("Sensor Listener");
        _handlerThread.start();
        _handler = new Handler(_handlerThread.getLooper());
    }

    private void createExerciseDirectory() {
//        File dir = getDir(_exerciseName, Context.MODE_PRIVATE);

        _directory = new File(getExternalFilesDir(null).getAbsolutePath() + File.separator + _exerciseName);
        Log.d("Storage path", getExternalFilesDir(null).getAbsolutePath());
        if (!_directory.exists()) {
            _directory.mkdirs();
            Log.d("External Dir", _directory.getAbsolutePath());
        }
    }

    private void createFile() throws IOException {
        repCount++;
        String filename = repCount + ".txt";
        File file = new File(_directory, filename);
        Log.d("Exercise File", file.getAbsolutePath());
//        File file = new File(Environment.DIRECTORY_PICTURES, filename);
        if(!file.exists()) {
            file.createNewFile();
        }
        showToast("File " + filename + " is created in " + getApplicationContext().getFilesDir());
        _foutStream = new ObjectOutputStream(new FileOutputStream(file));
    }

    private void saveFeatures() throws IOException, ClassNotFoundException {
        File file = new File(getExternalFilesDir(null), _exerciseName + ".txt");
        if(!file.exists()) {
            file.createNewFile();
        }
        ObjectOutputStream oostream = new ObjectOutputStream(new FileOutputStream(file));
        for(int i = 1; i <= _reps; i++) {
            File readingFile = new File(_directory, i + ".txt");
            FileInputStream fistream = new FileInputStream(readingFile);
            ObjectInputStream oistream = new ObjectInputStream(fistream);
            ArrayList<SensorReading> reading = new ArrayList<>();
            while (fistream.available() > 0) { // Check if the file stream is at the end
                reading.add((SensorReading)oistream.readObject());
            }
            // TODO: Get feature object here
            // Create ExerciseData object from the readings
            ExerciseData exerciseData = new ExerciseData(reading);
            Feature feature = new Feature();
            Log.d("reading_size", reading.size() + "");
            feature._features = exerciseData.getFeatureVector();
            feature._time = exerciseData.getTime();
            feature._classLabel = Globals._numExercises + 1;
            oostream.writeObject(feature);
        }
        if(Globals._exerciseLabels == null) {
            Globals._exerciseLabels = new HashMap<>();
        }
        Globals._exerciseLabels.put(_exerciseName, Globals._numExercises + 1);
        Globals._numExercises++;
        Globals.saveExerciseLabels(this);

        Classifier.trainModel(this);

    }
    private void hide_keep_discard_buttons() {
        _keepButton.setVisibility(View.INVISIBLE);
        _discardButton.setVisibility(View.INVISIBLE);
    }
    private void show_keep_discard_buttons() {
        _keepButton.setVisibility(View.VISIBLE);
        _discardButton.setVisibility(View.VISIBLE);
    }
    private void keep_or_discard(boolean keep) throws IOException, ClassNotFoundException {
        if(keep) {
            // save the current reading
            _foutStream.flush();
            _foutStream.close();
            upadteRepCount();
            if(repCount == _reps) {
                // Done with the work
                saveFeatures();
                // Launch MainActivity
                Intent mainActivityIntent = new Intent(TrainExerciseActivity.this,MainActivity.class);
                mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                TrainExerciseActivity.this.startActivity(mainActivityIntent);
            } else {
                _startButton.setVisibility(View.VISIBLE);
            }
        } else {
            // discard the current reading
            repCount--;
            _startButton.setVisibility(View.VISIBLE);
        }
        hide_keep_discard_buttons();
    }
    private void startRecording() throws IOException {
        // Start recording readings
        createFile();
        record();
        // Ask to keep or discard the reading
        show_keep_discard_buttons();
    }

    boolean _started;
    private double calculateAverage(ArrayList<Double> dataList) {
        double sum = 0;
        if(!dataList.isEmpty()) {
            for (Double elem : dataList) {
                sum += elem;
            }
            //Log.d("Sum is ","sum "+sum +" size"+dataList.size());
            return sum/dataList.size();
        }
        return sum;
    }


    public double getMean(ArrayList<Double> values) {
        double mean = 0.0;
        if ((values != null) && (values.size() > 0)) {
            for (double value : values) {
                mean += value;
            }
            mean /= values.size();
        }
        return mean;
    }

    public double getStandardDeviation(ArrayList<Double> values) {
        double deviation = 0.0;
        if ((values != null) && (values.size() > 1)) {
            double mean = getMean(values);
            for (double value : values) {
                double delta = value-mean;
                deviation += delta*delta;
            }
            deviation = Math.sqrt(deviation/values.size());
        }
        return deviation;
    }

    public void record() {
        try {
            _started = false;

            Log.d("Starting", "Checking the values");
            Log.d("Color", "Red");
            while(!_started) {
                if(magLinXList.size() < 50) {
                    // Acquiring data
                    acquireData();
                } else {
                    while (magLinXList.size() > 50) {
                        // Still the person hasn't moved his/her phone
                        // Therefore simply drop previous readings
                        magLinXList.remove(0);
                        magLinYList.remove(0);
                        magLinZList.remove(0);
                        removeReading();
                    }
                    acquireData();
                    //_started = checkMean()
                }
                Thread.sleep(10);
//                meanx = calculateAverage(magLinXList);
//                meany = calculateAverage(magLinYList);
//                meanz = calculateAverage(magLinZList);
                devx = getStandardDeviation(oriXList);
                devy = getStandardDeviation(oriYList);
                devz = getStandardDeviation(oriZList);
                if(magLinXList.size() > 50) {
                    if (devx > 0.9 || devy > 0.9 || devz > 0.9) {
                        Log.d("Linear X", "ACC " + _linX + " " + _linY + " " + _linZ);
                        Log.d("Means are", "Mean " + meanx + " " + meany + " " + meanz);
                        Log.d("RED", "RED is here");
                        _started = true;
                        break;
                    }
                }
            }
            while(_started) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Here we start recording
                if(magLinXList.size() < 100) {
                    acquireData();
                } else {
                    while(magLinXList.size() > 100) {
                        recordReading();
                        magLinXList.remove(0);
                        magLinYList.remove(0);
                        magLinZList.remove(0);
                    }
                    acquireData();
                    //_started = checkMean();
//                    meanx = calculateAverage(magLinXList);
//                    meany = calculateAverage(magLinYList);
//                    meanz = calculateAverage(magLinZList);
                    devx = getStandardDeviation(oriXList);
                    devy = getStandardDeviation(oriYList);
                    devz = getStandardDeviation(oriZList);

                    if (devx < 0.9 && devy < 0.9 && devz < 0.9) {
                        Log.d("Blue","Blue is here");
                        break;
                    }
                }

                Log.d("Linear X","ACC "+_linX + " "+_linY + " "+_linZ);
                Log.d("Means are","Mean "+meanx + " "+meany + " "+meanz);
            }
            Log.d("Color", "Blue");
            refreshDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception",e.getClass().toString() + " " +  e.getMessage(), e.getCause());
        } finally {
            try {
                Log.d("Somehow", "wth");
                winTrack.start();
            } catch(Exception a) {
            }
        }
    }

    public void showToast(final String toast) {
        Toast.makeText(TrainExerciseActivity.this, toast, Toast.LENGTH_LONG).show();
    }
    private void removeReading() {
        linXList.remove(0);
        linYList.remove(0);
        linZList.remove(0);
        gyrXList.remove(0);
        gyrYList.remove(0);
        gyrZList.remove(0);
        oriXList.remove(0);
        oriYList.remove(0);
        oriZList.remove(0);
    }
    private void recordReading() throws IOException {
        // Simply dumps one record in the file
        SensorReading currentReading = new SensorReading(linXList.remove(0),linYList.remove(0),
                linZList.remove(0),gyrXList.remove(0),gyrYList.remove(0),gyrZList.remove(0),
                oriXList.remove(0),oriYList.remove(0),oriZList.remove(0));
        _foutStream.writeObject(currentReading);
    }

    private void acquireData() {
        magLinXList.add(Math.abs(_linX));
        magLinYList.add(Math.abs(_linY));
        magLinZList.add(Math.abs(_linZ));

        linXList.add(_linX);
        linYList.add(_linY);
        linZList.add(_linZ);

        gyrXList.add(_gyrX);
        gyrYList.add(_gyrY);
        gyrZList.add(_gyrZ);

        oriXList.add(_oriX);
        oriYList.add(_oriY);
        oriZList.add(_oriZ);
    }

    private void upadteRepCount() {
        repsRemaining.setText("Number of Reps Remaining : " + (_reps - repCount));
    }

    private SensorEventListener _linearAccelerationListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            _linX = event.values[0];
            _linY = event.values[1];
            _linZ = event.values[2];
            //refreshDisplay();
        }
    };

    private SensorEventListener _gyroSensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            _gyrX = event.values[0];
            _gyrY = event.values[1];
            _gyrY = event.values[2];
            //refreshDisplay();
        }
    };
    private SensorEventListener _orientationSensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            _oriX = event.values[0];
            _oriY = event.values[1];
            _oriZ = event.values[2];
            //refreshDisplay();
        }
    };

    private void refreshDisplay() {
    }
    @Override
    protected void onResume() {
        super.onResume();
        _sensorManager.registerListener(_linearAccelerationListener, _linearAccelerationSensor,
                SensorManager.SENSOR_DELAY_FASTEST,_handler);
        _sensorManager.registerListener(_gyroSensorEventListener, _gyroSensor,
                SensorManager.SENSOR_DELAY_FASTEST,_handler);
        _sensorManager.registerListener(_orientationSensorListener, _orientationSensor,
                SensorManager.SENSOR_DELAY_FASTEST,_handler);
    }

    @Override
    protected void onStop() {
        _sensorManager.unregisterListener(_linearAccelerationListener);
        _sensorManager.unregisterListener(_orientationSensorListener);
        _sensorManager.unregisterListener(_gyroSensorEventListener);
        super.onStop();
    }
}
