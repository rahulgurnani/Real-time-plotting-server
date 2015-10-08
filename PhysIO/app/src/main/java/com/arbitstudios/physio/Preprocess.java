package com.arbitstudios.physio;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by nishkarsh_shastri on 07-Oct-15.
 */
public class Preprocess {

    public Preprocess()
    {}

    public static ExerciseData getFormattedObject(ArrayList<SensorReading> current){
        ExerciseData cur=new ExerciseData();

        Log.d("Initial Reading", "Starting reading");
        for(SensorReading o:current){
            String temp=o._linX+" "+o._linY+" "+o._linZ+" "+o._gyrX+" "+o._gyrY+" "+o._gyrZ+" "+o._oriX+" "+o._oriY+" "+o._oriZ;
            cur.append_data(temp);
        }
        Log.d("Objects Read","All objects");
        return cur;
    }

    public static ArrayList<Double> getFeatures(ArrayList<SensorReading> current) {
        ExerciseData newfeature = getFormattedObject(current);
        Log.d("Task2","Starting task2");
        newfeature = Classifier.collect_exercise_data(newfeature);
        Log.d("Task3","Starting task3");
        ArrayList<Double> features = Classifier.get_features_for_classifier(newfeature);
        return features;
    }
}
