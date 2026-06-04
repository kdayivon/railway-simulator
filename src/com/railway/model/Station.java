package com.railway.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Station implements Node {
    private final String id;
    private final String name;
    private final double x;
    private final double y;
    private final List<TrackSegment> connectedSegments;
    private final int capacity;

    public Station(String id, String name, double x, double y, int capacity) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
        this.connectedSegments = new ArrayList<>();
    }

    @Override
    public String getId() { return id; }
    public String getName() { return name; }
    
    @Override
    public double getX() { return x; }
    @Override
    public double getY() { return y; }

    @Override
    public List<TrackSegment> getConnectedSegments() {
        return connectedSegments;
    }
    
    public void addSegment(TrackSegment segment) {
        this.connectedSegments.add(segment);
    }
    
    public int getCapacity() { return capacity; }
}
