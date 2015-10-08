package com.arbitstudios.physio;

import libsvm.svm_model;

/**
 * Created by nishkarsh_shastri on 07-Oct-15.
 */
public class SVMmodel {
    public svm_model model;
    public SVMmodel(svm_model m) {
        this.model=m;
    }
}

