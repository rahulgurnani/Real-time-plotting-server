package com.arbitstudios.physio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ExerciseData {

    private HashMap<String, ArrayList<Double>> Ori;
    private HashMap<String, ArrayList<Double>> Acc;
    private HashMap<String, ArrayList<Double>> Gyr;
    public double CONST_K=0.1;
    public ArrayList<String> directions=new ArrayList<>(Arrays.asList("X", "Y", "Z"));
    public ExerciseData() {
        this.Ori=new HashMap();
        this.Acc=new HashMap();
        this.Gyr=new HashMap();

        this.Ori.put("X", new ArrayList<Double>());
        this.Acc.put("X", new ArrayList<Double>());
        this.Gyr.put("X", new ArrayList<Double>());
        this.Ori.put("Y", new ArrayList<Double>());
        this.Acc.put("Y", new ArrayList<Double>());
        this.Gyr.put("Y", new ArrayList<Double>());
        this.Ori.put("Z", new ArrayList<Double>());
        this.Acc.put("Z", new ArrayList<Double>());
        this.Gyr.put("Z", new ArrayList<Double>());

    }

    public HashMap<String, ArrayList<Double>> getAcc(){
        return Acc;
    }

    public HashMap<String, ArrayList<Double>> getOri(){
        return Ori;
    }

    public HashMap<String, ArrayList<Double>> getGyr(){
        return Gyr;
    }

    public void append_data(String data){
        String[] values=data.split(" ");

        this.Acc.get("X").add(Double.parseDouble(values[0]));
        this.Acc.get("Y").add(Double.parseDouble(values[1]));
        this.Acc.get("Z").add(Double.parseDouble(values[2]));
        this.Ori.get("X").add(Double.parseDouble(values[3]));
        this.Ori.get("Y").add(Double.parseDouble(values[4]));
        this.Ori.get("Z").add(Double.parseDouble(values[5]));
        this.Gyr.get("X").add(Double.parseDouble(values[6]));
        this.Gyr.get("Y").add(Double.parseDouble(values[7]));
        this.Gyr.get("Z").add(Double.parseDouble(values[8]));
    }

    public void reduce_to(int value){
        int interval=this.Acc.get("X").size()/value;
        HashMap<String, ArrayList<Double>> temp=new HashMap();

        temp.put("X", new ArrayList<Double>());
        temp.put("Y", new ArrayList<Double>());
        temp.put("Z", new ArrayList<Double>());

        int i=0;
        int cur=0;
        while(i<value)
        {
            for(String d:directions)
            {
                temp.get(d).add(this.Acc.get(d).get(cur));
            }
            cur+=interval;
            i++;
        }

        for(String d:directions)
        {
            this.Acc.put(d, temp.get(d));
        }

        temp.get("X").clear();
        temp.get("Y").clear();
        temp.get("Z").clear();
        i=0;
        cur=0;
        while(i<value)
        {
            for(String d:directions)
            {
                temp.get(d).add(this.Gyr.get(d).get(cur));
            }
            cur+=interval;
            i++;
        }

        for(String d:directions)
        {
            this.Gyr.put(d, temp.get(d));
        }

        temp.get("X").clear();
        temp.get("Y").clear();
        temp.get("Z").clear();
        i=0;
        cur=0;
        while(i<value)
        {
            for(String d:directions)
            {
                temp.get(d).add(this.Ori.get(d).get(cur));
            }
            cur+=interval;
            i++;
        }

        for(String d:directions) {
            this.Ori.put(d, temp.get(d));
        }
    }

    public double k_filtering(double previous, double current, double k_filtering_factor){
        return ((current*k_filtering_factor)+(previous*(1.0-k_filtering_factor)));
    }

    public void filter_data(){
        for(String d:directions)
        {
            for(int j=1;j<this.Acc.get(d).size();j++)
            {
                this.Acc.get(d).set(j, k_filtering(this.Acc.get(d).get(j-1),
                        this.Acc.get(d).get(j),
                        CONST_K));
                this.Ori.get(d).set(j, k_filtering(this.Ori.get(d).get(j-1),
                        this.Ori.get(d).get(j),
                        CONST_K));
                this.Gyr.get(d).set(j, k_filtering(this.Gyr.get(d).get(j-1),
                        this.Gyr.get(d).get(j),
                        CONST_K));
            }
        }
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

    public void normalize(){
        double diff_max=0;
        for(String d:directions)
        {
            diff_max=Math.max(diff_max, Collections.max(this.Acc.get(d))-Collections.min(this.Acc.get(d)));
        }
        for(String d:directions)
        {
            int mean=(int)getAvg(this.Acc.get(d));
            for(int i=0;i<this.Acc.get(d).size();i++){
                this.Acc.get(d).set(i, (this.Acc.get(d).get(i)-mean)/diff_max);
            }
        }

        diff_max=0;
        for(String d:directions)
        {
            diff_max=Math.max(diff_max, Collections.max(this.Gyr.get(d))-Collections.min(this.Gyr.get(d)));
        }
        diff_max=diff_max==0?1:diff_max;
        for(String d:directions)
        {
            int mean=(int)getAvg(this.Gyr.get(d));
            for(int i=0;i<this.Gyr.get(d).size();i++){
                this.Gyr.get(d).set(i, (this.Gyr.get(d).get(i)-mean)/diff_max);
            }
        }

        diff_max=0;
        for(String d:directions)
        {
            diff_max=Math.max(diff_max, Collections.max(this.Ori.get(d))-Collections.min(this.Ori.get(d)));
        }
        diff_max=diff_max==0?1:diff_max;
        for(String d:directions)
        {
            for(int i=0;i<this.Ori.get(d).size();i++){
                this.Ori.get(d).set(i, this.Ori.get(d).get(i)/diff_max);
            }
        }
    }


}
