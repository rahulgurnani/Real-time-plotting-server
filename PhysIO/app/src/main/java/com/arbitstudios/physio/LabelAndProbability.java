package com.arbitstudios.physio;

public class LabelAndProbability {
    public int _label;
    public double _probability;

    public LabelAndProbability() {
        _label = 0;
        _probability = 0.0;
    }
    public LabelAndProbability(int label, double probability) {
        _label = label;
        _probability = probability;
    }
}
