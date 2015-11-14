package com.arbitstudios.physio;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ashutosh on 07/10/2015.
 */
public class Globals {
    public static int _numExercises;
    public static final String EXERCISES_FILE_NAME = "Exercises.txt";
    public static final String MODEL_FILE_NAME = "Model.txt";
    public static Map<String, Integer> _exerciseLabels;
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    public static void loadExerciseLabels(MainActivity obj) throws IOException, ClassNotFoundException {
        File file = new File(obj.getExternalFilesDir(null), EXERCISES_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        _exerciseLabels = (HashMap) ois.readObject();
        if(_exerciseLabels == null || _exerciseLabels.isEmpty()) {
            _exerciseLabels = new HashMap<>();
        }
        ois.close();
    }

    public static void saveExerciseLabels(TrainExerciseActivity obj) throws IOException {
        File file = new File(obj.getExternalFilesDir(null), EXERCISES_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(file));
        oout.writeObject(_exerciseLabels);
        oout.close();
    }

    public static ArrayList<Feature> getAllExerciseFeatures(ActionBarActivity obj, String exerciseName) throws IOException, ClassNotFoundException {
        File file = new File(obj.getExternalFilesDir(null), exerciseName + ".txt");
        return getFeaturesFromFile(file);
    }

    private static ArrayList<Feature> getFeaturesFromFile(File file) throws IOException, ClassNotFoundException {
        ArrayList<Feature> featuresFromFile = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        while(fis.available() > 0) {
            featuresFromFile.add((Feature) ois.readObject());
        }
        fis.close();
        ois.close();
//        for (Feature f : featuresFromFile) {
//            Log.d("Features read", file.getName() + " : " + f._classLabel);
//        }
        return featuresFromFile;
    }
    public static ArrayList<Feature> getAllFeatures(ActionBarActivity obj) throws IOException, ClassNotFoundException {
        File currentDir = obj.getExternalFilesDir(null);
        File[] fileList = currentDir.listFiles();
        ArrayList<Feature> allFeatures = new ArrayList<>();
        for (File file : fileList) {
            if (file.isFile()) {
                if (file.getName().equals(EXERCISES_FILE_NAME) || file.getName().equals(MODEL_FILE_NAME)) {
                    continue;
                }
                allFeatures.addAll(getFeaturesFromFile(file));
            }
        }
        for (Feature f : allFeatures) {
            Log.d("Features read", " : " + f._classLabel);
        }
        return allFeatures;
    }

}
