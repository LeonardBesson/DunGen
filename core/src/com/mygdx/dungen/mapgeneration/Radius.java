package com.mygdx.dungen.mapgeneration;


public class Radius {
    private float value;
    private boolean isAboveThreshold;

    public Radius(float value, boolean isAboveThreshold) {
        this.value = value;
        this.isAboveThreshold = isAboveThreshold;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isAboveThreshold() {
        return isAboveThreshold;
    }

    public void setAboveThreshold(boolean aboveThreshold) {
        isAboveThreshold = aboveThreshold;
    }
}
