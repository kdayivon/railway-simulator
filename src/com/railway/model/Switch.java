package com.railway.model;

import java.util.ArrayList;
import java.util.List;

public class Switch implements Node {
    private final String id;
    private final double x;
    private final double y;
    private final List<TrackSegment> connectedSegments;

    public Switch(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.connectedSegments = new ArrayList<>();
    }

    @Override
    public String getId() { return id; }

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
}
