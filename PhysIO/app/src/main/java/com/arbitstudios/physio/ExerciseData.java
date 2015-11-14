package com.arbitstudios.physio;

import java.util.ArrayList;

public class ExerciseData {
    public static final int N_SAMPLES = 50;
    private int _time;
    // Array of arraylists for linear acceleration, gyrometer readings and orientation sensor readings
    private ArrayList<ArrayList<Double>> _lins;
    private ArrayList<ArrayList<Double>> _gyrs;
    private ArrayList<ArrayList<Double>> _oris;
    private ArrayList<ArrayList<Double>> _gradientLins;
    private ArrayList<ArrayList<Double>> _gradientGyrs;
    private ArrayList<ArrayList<Double>> _gradientOris;
    public double CONST_K=0.1;
    public ExerciseData() {
        _lins = new ArrayList<ArrayList<Double>>();
        _gyrs = new ArrayList<ArrayList<Double>>();
        _oris = new ArrayList<ArrayList<Double>>();
        _gradientLins = new ArrayList<ArrayList<Double>>();
        _gradientGyrs = new ArrayList<ArrayList<Double>>();
        _gradientOris = new ArrayList<ArrayList<Double>>();

        // Creating individual arraylists for readings in each direction X,Y and Z
        for(int i = 0; i < 3; i++) {
            _lins.add(new ArrayList<Double>());
            _gyrs.add(new ArrayList<Double>());
            _oris.add(new ArrayList<Double>());
        }
    }
    public ExerciseData(ArrayList<SensorReading> readings) {
        this();
        _time = readings.size();
        for (SensorReading reading : readings) {
            this.append_data(reading);
        }
    }

    public void append_data(SensorReading newReading) {
        // Appending the sensor reading into individual arraylist
        // Linear Acceleration
        _lins.get(Globals.X).add(newReading._linX);
        _lins.get(Globals.Y).add(newReading._linY);
        _lins.get(Globals.Z).add(newReading._linZ);
        // Gyrometer
        _gyrs.get(Globals.X).add(newReading._gyrX);
        _gyrs.get(Globals.Y).add(newReading._gyrY);
        _gyrs.get(Globals.Z).add(newReading._gyrZ);
        // Orientation
        _oris.get(Globals.X).add(newReading._oriX);
        _oris.get(Globals.Y).add(newReading._oriY);
        _oris.get(Globals.Z).add(newReading._oriZ);
    }

    public ArrayList<Double> sampleList(ArrayList<Double> list, int samples) {
        double size = list.size();
        ArrayList<Double> sampledList = new ArrayList<>();
        for (int i = 0; i < samples; i++) {
            sampledList.add(list.get((int)((double)i*size/(double)samples)));
        }
        return sampledList;
    }

    public void sampleAll(int samples) {
        // Sample all the arrays to the number of samples provided
        for(int i = 0; i < 3; i++) {
            _lins.set(i,sampleList(_lins.get(i),samples));
            _gyrs.set(i,sampleList(_gyrs.get(i),samples));
            _oris.set(i, sampleList(_oris.get(i), samples));
            _gradientLins.set(i, sampleList(_gradientLins.get(i), samples));
            _gradientGyrs.set(i, sampleList(_gradientGyrs.get(i), samples));
            _gradientOris.set(i, sampleList(_gradientOris.get(i), samples));
        }
    }

    public double k_filtering(double previous, double current, double k_filtering_factor) {
        return ((current*k_filtering_factor)+(previous*(1.0-k_filtering_factor)));
    }

    public void filterLinearAcceleration() {
        // Smooth each direction of linear acceleration data
        for (int i = 0; i < 3; i++) {
            ArrayList<Double> lins = _lins.get(i);
            for (int j = 1; j < lins.size(); j++) {
                lins.set(j, k_filtering(lins.get(j - 1), lins.get(j), CONST_K));
            }
        }
    }

    public void filterGyrometer() {
        // Smooth each direction of gyrometer data
        for (int i = 0; i < 3; i++) {
            ArrayList<Double> gyrs = _gyrs.get(i);
            for (int j = 1; j < gyrs.size(); j++) {
                gyrs.set(j, k_filtering(gyrs.get(j - 1), gyrs.get(j), CONST_K));
            }
        }
    }

    public void filterOrientation() {
        // Smooth each direction of orientation data
        for (int i = 0; i < 3; i++) {
            ArrayList<Double> oris = _oris.get(i);
            for (int j = 1; j < oris.size(); j++) {
                oris.set(j, k_filtering(oris.get(j - 1), oris.get(j), CONST_K));
            }
        }
    }

    public void filterAll() {
        filterLinearAcceleration();
        filterGyrometer();
        filterOrientation();
    }

    public double getAvg(ArrayList<Double> numbers){
        Double sum = 0.0;
        if(!numbers.isEmpty()) {
            for (Double num : numbers) {
                sum += num;
            }
            return sum / numbers.size();
        }
        return sum;
    }

    public ArrayList<Double> integrate(ArrayList<Double> readingsList){
        //simple implementation of numpy.trapz(x) function
        ArrayList<Double> integration = new ArrayList<>();
        // First value of integration is zero
        integration.add(0.0);
        // Adding the second value manually
        integration.add((readingsList.get(0) + readingsList.get(1)) / 2.0);

        double height = 1; //default dx = 1
        double second_to_second_last_sum = 0.0;
        for(int i = 2; i < readingsList.size(); i++) {
            double first_last_sum = readingsList.get(0) + readingsList.get(i);
            second_to_second_last_sum += readingsList.get(i-1);
            integration.add(height * (first_last_sum + 2.0*second_to_second_last_sum) / 2.0);
        }
        return integration;
    }

    private double mod360(double x) {
        if (x < 0.0) {
            x += 360.0;
        }
        if (x > 360.0) {
            x -= 360.0;
        }
        return x;
    }

    public void normalizeOrientation() {
        // In orientation the Azimuth is dependant upon the angle of the user
        /* In our case this is useless since, the person should not have restriction upon direction
         * he/she is facing */
        ArrayList<Double> azimuth = _oris.get(0);
        if (azimuth.size() <= 1)
            // Should not happen
            return;
        double startingAzimuth = azimuth.get(0);
        // Azimuth varies from 0 to 360
        for (int i = 0; i < azimuth.size(); i++) {
            azimuth.set(i, (mod360(azimuth.get(i) - startingAzimuth)/180.0));
        }
    }

    public ArrayList<Double> gradient(ArrayList<Double> data) {
        //np.gradient(x) implementation
        ArrayList<Double> myGradients = new ArrayList<Double>();
        myGradients.add(data.get(1) - data.get(0)/2);
        for(int i = 1; i < data.size(); i++) {
            if(i == data.size()-1) {
                myGradients.add((data.get(i)-data.get(i-1))/2);
            } else {
                myGradients.add((data.get(i+1)-data.get(i-1))/2);
            }
        }
        return  myGradients;
    }
    public void calculateGradients() {
        for (int i = 0; i < 3; i++) {
            _gradientLins.add(gradient(_lins.get(i)));
            _gradientGyrs.add(gradient(_gyrs.get(i)));
            _gradientOris.add(gradient(_oris.get(i)));
        }
    }
    public ArrayList<Double> getFeatureVector() {
        // Fix Azimuth
        normalizeOrientation();
        // Integrate Acceleration
        for( int i = 0; i < 3; i++) {
            _lins.set(i, integrate(_lins.get(i)));
        }
        // Smooth all readings
        filterAll();
        // Evaluate Gradients
        calculateGradients();
        // Sample All magnitudes and gradients
        sampleAll(N_SAMPLES);
        // Append all samples to final feature arraylist
        ArrayList<Double> featureVector = new ArrayList<>();
        for( int i = 0; i < 3; i++) {
            featureVector.addAll(_lins.get(i));
            featureVector.addAll(_gyrs.get(i));
            featureVector.addAll(_oris.get(i));
            featureVector.addAll(_gradientLins.get(i));
            featureVector.addAll(_gradientGyrs.get(i));
            featureVector.addAll(_gradientOris.get(i));
        }
        return featureVector;
    }
    public int getTime() {
        return _time;
    }
}
