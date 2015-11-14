package com.arbitstudios.physio;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import libsvm.svm_model;

public class ExerciseActivity extends ActionBarActivity {
    String _exerciseName;
    int _exerciseLabel;
    int _repCount;
    int _reps;

//    TextView _repsRemainingLabel;
    TextView _timeText;
//    Spinner _repsSpinner;
//    TextView _repsLabel;
    ImageButton _startExerciseBtn;

    // Media elements
    MediaPlayer countdownTrack;
    MediaPlayer winTrack;
    MediaPlayer failTrack;
    // Reading Variables
    private double _linX, _linY, _linZ;
    private double _gyrX, _gyrY, _gyrZ;
    private double _oriX, _oriY, _oriZ;
    private double meanx=-1,meany=-1,meanz=-1;
    private double devx=-1,devy=-1,devz=-1;
    private int threshold = 5000;
    private double minimum_similarity=0.0;

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

    // Sensor Readings
    ArrayList<SensorReading> _reading;

    // Sensor Variable
    private SensorManager _sensorManager;
    private Sensor _linearAccelerationSensor;
    private Sensor _gyroSensor;
    private Sensor _orientationSensor;
    boolean accStarted;

    // Sensor handler thread variable
    HandlerThread _handlerThread;
    Handler _handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent inputIntent = getIntent();
        _exerciseName = inputIntent.getStringExtra("ExerciseName");
        _exerciseLabel = inputIntent.getIntExtra("ExerciseLabel", 0);
        setTitle(_exerciseName + " " + _exerciseLabel);

        // Initialize view elements
//        _repsLabel = (TextView) findViewById(R.id.exercise_reps_label);
//        _repsSpinner = (Spinner) findViewById(R.id.exercise_reps_spinner);
//        _repsRemainingLabel = (TextView) findViewById(R.id.reps_remaining_label);
        _startExerciseBtn = (ImageButton) findViewById(R.id.start_exercise_btn);
        _timeText = (TextView) findViewById(R.id.timeText);
        // Initialize the _repsSpinner default entries
//        _repsSpinner = (Spinner) findViewById(R.id.exercise_reps_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.exercise_reps,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//       _repsSpinner.setAdapter(adapter);

        // Initialize UI
//        showButtonAndSpinner();

        // Initialize the Sensors
        _sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        _linearAccelerationSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        _gyroSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        _orientationSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        _handlerThread = new HandlerThread("Sensor Listener");
        _handlerThread.start();
        _handler = new Handler(_handlerThread.getLooper());

        // Initialize other variables
        _reading = new ArrayList<>();

        // Initializing media files
        countdownTrack = MediaPlayer.create(getApplicationContext(), R.raw.countdown);
        winTrack = MediaPlayer.create(getApplicationContext(), R.raw.win);
        failTrack = MediaPlayer.create(getApplicationContext(), R.raw.fail);
        // MediaFile on complete listeners

        // Start button on click listener
        countdownTrack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    Log.d("StartingEx", "StartingEX");
                    startExercise();
                    Log.d("StopEx", "StopEX");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        _startExerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _timeText.setText("");
                countdownTrack.start();
                _startExerciseBtn.setVisibility(View.INVISIBLE);
                //TODO: start animation
            }
        });

        // Spinner onchange listener
        /*_repsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _reps = Integer.parseInt(_repsSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                _reps = 0;
            }
        });*/

    }

    private void updateRepCount() {
//        _repsRemainingLabel.setText("Number of Reps Remaining : " + (_reps - _repCount));
    }

    private double DTWDistance(ArrayList<Double> p, ArrayList<Double> a) {
        double[][] DTW = new double[p.size()][a.size()];

        for (int i =0;i<p.size();i++)
        {
            DTW[i][0]=Double.MAX_VALUE;
        }
        for (int i =0;i<a.size();i++)
        {
            DTW[0][i]=Double.MAX_VALUE;
        }
        DTW[0][0]=0;

        for(int i=1;i<p.size();i++)
        {
            for(int j=1;j<a.size();j++)
            {
                double cost=Math.abs(p.get(i)-a.get(j));
                DTW[i][j]=cost+Math.min(DTW[i-1][j], Math.min(DTW[i][j-1], DTW[i-1][j-1]));
            }
        }
        return DTW[p.size()-1][a.size()-1];
    }

    private boolean checkClassification(ArrayList<ArrayList<Double>> p_list, ArrayList<Double> a_list, int predicted, int actual){
        double mean=0.0;
        minimum_similarity=0.0;
        for(int i=0;i<p_list.size();i++){
            double dist=DTWDistance(p_list.get(i), a_list);
            minimum_similarity+=dist;
            Log.d("DTWDistance", "DTWDistance for tuple "+i+" ;is " + dist);
        }
        minimum_similarity/=p_list.size();
        if(minimum_similarity<threshold)return true;
        else return false;
    }

    private void startExercise() throws IOException, ClassNotFoundException {
        _repCount = 0;
        updateRepCount();
        _reading.clear();
        record();
        ExerciseData test = new ExerciseData(_reading);
        int time = test.getTime();
        int mean_time=0;
        Log.d("StartingEx", "FeatVect");
        ArrayList<Double> feature_test = test.getFeatureVector();

        ArrayList<ArrayList<Double>> feature_tocompare = new ArrayList<ArrayList<Double>>();
        ArrayList<Feature> trained_features;
        // We want exercise name

        trained_features = Globals.getAllExerciseFeatures(this, _exerciseName);

        Log.d("StartingEx", "Class");
        LabelAndProbability result = Classifier.evaluate(feature_test);

        for (Feature f:trained_features){
//                Log.d("StartingEX", "Features "+f._features.size());
            mean_time+=f._time;
            feature_tocompare.add(f._features);
        }

        mean_time/=trained_features.size();
        Log.d("TimeDebug", "Mean Time :"+mean_time+" Test Time:"+time);
        int label = result._label;
        if ((time>=0.6*mean_time && time <=1.4*mean_time) && label==_exerciseLabel && checkClassification(feature_tocompare, feature_test, label, _exerciseLabel)) {
            // WIN
            winTrack.start();
            Toast.makeText(this, "Exercise = " + label + " Similarity = " + minimum_similarity, Toast.LENGTH_LONG).show();
            _repCount++;
        } else if (time < 0.6*mean_time) {
            _timeText.setBackgroundColor(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
            _timeText.setText("Too Fast");
            failTrack.start();
        } else if (time > 1.4*mean_time) {
            _timeText.setBackgroundColor(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
            _timeText.setText("Too Slow");
            failTrack.start();
        } else {
                // FAIL
                Toast.makeText(this, "Exercise = " + label + " Similarity = " + minimum_similarity, Toast.LENGTH_LONG).show();
                failTrack.start();
        }

        _startExerciseBtn.setVisibility(View.VISIBLE);
    }

    /*private void hideButtonAndSpinner() {
        // Function to be called at the start of the exercise
        // Hide start button and spinner
        _repsSpinner.setVisibility(View.INVISIBLE);
        _repsLabel.setVisibility(View.INVISIBLE);
        _startExerciseBtn.setVisibility(View.INVISIBLE);

        // Show reps remaining label
        _repsRemainingLabel.setVisibility(View.VISIBLE);
    }
    private void showButtonAndSpinner() {
        // Function to be called at the end of the exercise
        // Hide start button and spinner
        _repsSpinner.setVisibility(View.VISIBLE);
        _repsLabel.setVisibility(View.VISIBLE);
        _startExerciseBtn.setVisibility(View.VISIBLE);

        // Show reps remaining label
        _repsRemainingLabel.setVisibility(View.INVISIBLE);
    }*/

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
        // Simply add the reading in the global arraylist
        _reading.add(new SensorReading(linXList.remove(0),linYList.remove(0),
                linZList.remove(0),gyrXList.remove(0),gyrYList.remove(0),gyrZList.remove(0),
                oriXList.remove(0),oriYList.remove(0),oriZList.remove(0)));
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

        boolean started;
        try {
            started = false;

            Log.d("Starting", "Checking the values");
            Log.d("Color", "Red");
            while(!started) {
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
//                        Log.d("Linear X", "ACC " + _linX + " " + _linY + " " + _linZ);
//                        Log.d("Means are", "Mean " + meanx + " " + meany + " " + meanz);
//                        Log.d("RED", "RED is here");
                        started = true;
                        break;
                    }
                }
            }
            while(started) {
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

//                Log.d("Linear X","ACC "+_linX + " "+_linY + " "+_linZ);
//                Log.d("Means are","Mean "+meanx + " "+meany + " "+meanz);
            }
            Log.d("Color", "Blue");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception",e.getClass().toString() + " " +  e.getMessage(), e.getCause());
        } finally {
            try {
                Log.d("Exercise Finish", "yoyoyo");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        _sensorManager.registerListener(_linearAccelerationListener, _linearAccelerationSensor,
                SensorManager.SENSOR_DELAY_FASTEST, _handler);
        _sensorManager.registerListener(_gyroSensorEventListener, _gyroSensor,
                SensorManager.SENSOR_DELAY_FASTEST, _handler);
        _sensorManager.registerListener(_orientationSensorListener, _orientationSensor,
                SensorManager.SENSOR_DELAY_FASTEST, _handler);
    }

    @Override
    protected void onStop() {
        _sensorManager.unregisterListener(_linearAccelerationListener);
        _sensorManager.unregisterListener(_orientationSensorListener);
        _sensorManager.unregisterListener(_gyroSensorEventListener);
        super.onStop();
    }


}
