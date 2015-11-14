package com.arbitstudios.physio;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by nishkarsh_shastri on 07-Oct-15.
 */
public class Feature implements Serializable{

    public int _classLabel;
    public int _time;
    public ArrayList<Double> _features;
    public Feature() {
        _features = new ArrayList<>();
    }
    public Feature(int L, ArrayList<Double> readings){
        this._classLabel = L;
        this._features = readings;
    }
}
