package com.arbitstudios.physio;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import libsvm.*;


public class Classifier {

    private ArrayList<Double> my_data;
    private int classes;
    //Need to define the svm classifier datatype. Don't know how to do that in libsvm
    public static ArrayList<String> directions=new ArrayList<>(Arrays.asList("X","Y","Z"));
    public static ArrayList<Double> extract_magnitudes(ArrayList<Double> data, int nsamples)
    {
        int size = data.size();
        ArrayList<Double> samples = new ArrayList<Double>();
        for (int i=0; i<data.size(); i++)
        {
            samples.add(data.get(i*size/nsamples));
        }
        return samples;
    }

    public static ArrayList<Double> extract_gradient(ArrayList<Double> data, int nsamples)
    {
        //np.gradient(x) implementation
        ArrayList<Double> myGradients = new ArrayList<Double>();
        myGradients.set(0,(data.get(1)-data.get(0)/2));
        for(int i=1;i<data.size();i++)
        {
            if(i==data.size()-1)
            {
                myGradients.set(i,(data.get(i)-data.get(i-1))/2);
            }
            else
            {
                myGradients.set(i,(data.get(i+1)-data.get(i-1))/2);
            }
        }
        //now use this gradient obtained
        ArrayList<Double> samples = new ArrayList<Double>();
        for(int i=0;i<nsamples;i++)
        {
            samples.add(i,data.get(i*data.size()/nsamples));
        }
        return  samples;
    }

    public Classifier()
    {
        this.my_data = null;
        this.classes = -1;
        // How to set linear classifier using libsvm??
    }

    public static double trapz(List<Double> subReadings){

        //simple implementation of numpy.trapz(x) function
        double height = 1;//default dx = 1
        double first_last_sum = subReadings.get(0)+subReadings.get(subReadings.size()-1);
        double second_to_second_last_sum=0;
        for(int i=1;i<subReadings.size()-1;i++)
        {
            second_to_second_last_sum+=subReadings.get(i);
        }
        second_to_second_last_sum*=2;

        return height*(first_last_sum+second_to_second_last_sum)/2;

    }

    public static ExerciseData collect_exercise_data(ExerciseData before) {
        ExerciseData reading=before;

        for(String d:directions)
        {
            ArrayList<Double> temp = new ArrayList<Double>();
            for(int j=0;j<reading.getAcc().size();j++)
            {
                temp.add(trapz(reading.getAcc().get(d).subList(0, j)));
            }
            reading.getAcc().put(d, temp);
            temp.clear();
            for(int j=0;j<reading.getAcc().size();j++)
            {
                temp.add(trapz(reading.getAcc().get(d).subList(0,j)));
            }
            reading.getAcc().put(d, temp);
        }

        reading.reduce_to(100);
        reading.filter_data();
        reading.normalize();

        return reading;
    }

    public static svm_model svmTrain(ArrayList<ArrayList<Double>> train)
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
                node.index = j;
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
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = 0.001;

        svm_model model = svm.svm_train(prob, param);

        return model;
    }

    public static double evaluate(ArrayList<Double> features, svm_model model)
    {
        svm_node[] nodes = new svm_node[features.size()-1];
        for (int i = 1; i < features.size(); i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features.get(i);

            nodes[i-1] = node;
        }

        int totalClasses = 2;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model,labels);

        double v = svm.svm_predict(model, nodes);

        return v;
    }

    public static ArrayList<Double> get_features_for_classifier(ExerciseData exercise){
        int nsamples =50;
        ArrayList<Double> reading= new ArrayList<>();

        for(String d:directions){
            reading.addAll(extract_magnitudes(exercise.getAcc().get(d), nsamples));
            reading.addAll(extract_gradient(exercise.getAcc().get(d), nsamples));
        }
        for (String d : directions){
            if(d=="Z")continue;
            reading.addAll(extract_magnitudes(exercise.getGyr().get(d), nsamples));
            reading.addAll(extract_gradient(exercise.getGyr().get(d), nsamples));
        }
        for (String d : directions){
            if(d=="Z")continue;
            reading.addAll(extract_magnitudes(exercise.getOri().get(d), nsamples));
            reading.addAll(extract_gradient(exercise.getOri().get(d), nsamples));
        }

        return reading;
    }

    public static svm_model trainSVM(ArrayList<Feature> features){
        ArrayList<ArrayList<Double>> train=new ArrayList<ArrayList<Double>>();
        ArrayList<Double> temp=new ArrayList<>();
        for(Feature f: features){
            temp.clear();
            temp.add((double) f._classLabel);
            temp.addAll(f._features);
            train.add(temp);
        }
        svm_model model=svmTrain(train);

        return model;
    }

    public static void writeModeltoFile(svm_model model){
        //Write SVM Model
        SVMmodel M = new SVMmodel(model);

        File sdcard = Environment.getExternalStorageDirectory();
        File ModelFile = new File(sdcard,"Model.txt");
        try{

            FileOutputStream fout = new FileOutputStream(ModelFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(M);
            oos.close();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static svm_model readModelfromFile(){
        SVMmodel M;

        File sdcard = Environment.getExternalStorageDirectory();
        File ModelFile = new File(sdcard,"Model.txt");
        try{

            FileInputStream fin = new FileInputStream(ModelFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            M = (SVMmodel) ois.readObject();
            ois.close();

            return M.model;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}
