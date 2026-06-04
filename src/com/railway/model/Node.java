package com.railway.model;

import java.util.List;

public interface Node {
    String getId();
    double getX();
    double getY();
    List<TrackSegment> getConnectedSegments();
}
