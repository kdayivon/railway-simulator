package com.railway.model;

public enum TrainType {
    LOCAL(20.0),   // m/s
    EXPRESS(50.0); // m/s

    private final double maxSpeed;

    TrainType(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }
}
