package com.arbitstudios.physio;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import libsvm.*;

public class Classifier {

    private ArrayList<Double> _my_data = null;
    private int _classes = -1;
    public static svm_model _model;
    //Need to define the svm classifier datatype. Don't know how to do that in libsvm
    public static ArrayList<String> directions = new ArrayList<>(Arrays.asList("X","Y","Z"));

    public Classifier()
    {
        this._my_data = null;
        this._classes = -1;
        // How to set linear classifier using libsvm??
    }

    public static void svmTrain(ArrayList<ArrayList<Double>> train)
    {
        svm_problem prob = new svm_problem();
        int dataCount = train.size();
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];

        for (int i = 0; i < dataCount; i++){
            ArrayList<Double> features = train.get(i);
            prob.x[i] = new svm_node[features.size()-1];
            for (int j = 1; j < features.size(); j++){
                svm_node node = new svm_node();
                node.index = j-1;
                node.value = features.get(j);
                prob.x[i][j-1] = node;
            }
            prob.y[i] = features.get(0);
        }

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.NU_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = 0.001;

        _model = svm.svm_train(prob, param);
    }

    public static int evaluate(ArrayList<Double> features)
    {
        svm_node[] nodes = new svm_node[features.size()-1];
        for (int i = 1; i < features.size(); i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features.get(i);

            nodes[i-1] = node;
        }

        int[] labels = new int[Globals._numExercises];
        double[] probabilities = new double[Globals._numExercises];
        svm.svm_get_labels(_model, labels);
        svm.svm_predict_probability(_model, nodes, probabilities);

        double max = 0.0;
        int label = 0;
        for (int i = 0; i < probabilities.length; i++) {
            Log.d("Labels and Probabilites", labels[i] + " : " + probabilities[i]);
            if (max < probabilities[i]) {
                label = labels[i];
                max = probabilities[i];
            }
        }
        return label;
    }

    public static void trainModel(TrainExerciseActivity obj) throws IOException, ClassNotFoundException {
        ArrayList<Feature> features = Globals.getAllFeatures(obj);
        ArrayList<ArrayList<Double>> train = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> temp = new ArrayList<>();
        for(Feature f: features) {
            temp.clear();
            temp.add((double) f._classLabel);
            temp.addAll(f._features);
            train.add(temp);
        }
        svmTrain(train);
        // Printing labels to check
        printModelLabels();
        writeModeltoFile(obj);
    }
    public static void printModelLabels() {
        int[] labels = _model.label;
        for (int i = 0; i < labels.length; i++) {
            Log.d("Training Labels", labels[i] + "");
        }
        int totalclasses = svm.svm_get_nr_class(_model);
        Log.d("Total Classes", totalclasses + "");
        labels = new int[totalclasses];
        svm.svm_get_labels(_model, labels);

        for (int i = 0; i < labels.length; i++) {
            Log.d("Training Labels", labels[i] + "");
        }
        Log.d("Model Type", svm.svm_get_svm_type(_model) + "");
    }
    public static void writeModeltoFile(TrainExerciseActivity obj) {
        //Write SVM Model

        File sdcard = obj.getExternalFilesDir(null);
        File ModelFile = new File(sdcard,Globals.MODEL_FILE_NAME);
        try {
            FileOutputStream fout = new FileOutputStream(ModelFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(_model);
            oos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void readModelfromFile(MainActivity obj) {
        File sdcard = obj.getExternalFilesDir(null);
        File ModelFile = new File(sdcard,Globals.MODEL_FILE_NAME);
        try {
            FileInputStream fin = new FileInputStream(ModelFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            _model = (svm_model) ois.readObject();
            ois.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
