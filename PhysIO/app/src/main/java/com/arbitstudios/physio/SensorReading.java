package com.arbitstudios.physio;

import java.io.Serializable;

/**
 * Created by Ashutosh on 02/10/2015.
 */
public class SensorReading implements Serializable {
    public double _linX, _linY, _linZ;
    public double _gyrX, _gyrY, _gyrZ;
    public double _oriX, _oriY, _oriZ;

    public SensorReading() {
        _linX = 0.0;
        _linY = 0.0;
        _linZ = 0.0;
        _gyrX = 0.0;
        _gyrY = 0.0;
        _gyrZ = 0.0;
        _oriX = 0.0;
        _oriY = 0.0;
        _oriZ = 0.0;
    }
    public SensorReading(double linX, double linY, double linZ, double gyrX, double gyrY, double gyrZ, double oriX,
                         double oriY, double oriZ) {
        _linX = linX;
        _linY = linY;
        _linZ = linZ;
        _gyrX = gyrX;
        _gyrY = gyrY;
        _gyrZ = gyrZ;
        _oriX = oriX;
        _oriY = oriY;
        _oriZ = oriZ;
    }
}
