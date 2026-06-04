package com.railway.model;

public class Train {
    private final String id;
    private final TrainType type;
    private TrackSegment currentSegment;
    
    // Physics variables
    private double positionOnSegment; // from 0 to currentSegment.getLength()
    private double currentVelocity;
    private Route route;
    
    // Direction: true = traveling A→B, false = traveling B→A on the current segment
    private volatile boolean forwardOnSegment;
    
    private boolean loopRoute;

    public Train(String id, TrainType type) {
        this(id, type, null, false);
    }

    public Train(String id, TrainType type, Route route) {
        this(id, type, route, false);
    }
    
    public Train(String id, TrainType type, Route route, boolean loopRoute) {
        this.id = id;
        this.type = type;
        this.route = route;
        this.loopRoute = loopRoute;
        this.positionOnSegment = 0;
        this.currentVelocity = 0;
    }

    public boolean isLoopRoute() { return loopRoute; }
    public void setLoopRoute(boolean loopRoute) { this.loopRoute = loopRoute; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getId() { return id; }
    public TrainType getType() { return type; }

    public TrackSegment getCurrentSegment() { return currentSegment; }
    public void setCurrentSegment(TrackSegment currentSegment) {
        this.currentSegment = currentSegment;
    }

    public double getPositionOnSegment() { return positionOnSegment; }
    public void setPositionOnSegment(double positionOnSegment) {
        this.positionOnSegment = positionOnSegment;
    }

    public double getCurrentVelocity() { return currentVelocity; }
    public void setCurrentVelocity(double currentVelocity) {
        this.currentVelocity = currentVelocity;
    }

    public boolean isForwardOnSegment() { return forwardOnSegment; }
    public void setForwardOnSegment(boolean forwardOnSegment) {
        this.forwardOnSegment = forwardOnSegment;
    }
}
