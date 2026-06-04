package com.railway.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class TrackSegment {
    private final String id;
    private final Node nodeA;
    private final Node nodeB;
    private final double length;
    
    // Two directional semaphores — one per lane.
    // A→B lane and B→A lane can be used simultaneously by different trains.
    private final Semaphore semaphoreAB;  // forward lane (A→B)
    private final Semaphore semaphoreBA;  // backward lane (B→A)
    
    // Per-lane occupation state for the UI
    private volatile boolean occupiedForward;
    private volatile boolean occupiedBackward;
    private volatile Train occupyingTrainForward;
    private volatile Train occupyingTrainBackward;

    // Observer list — CopyOnWriteArrayList is thread-safe for concurrent notification
    private final List<SegmentStateListener> listeners = new CopyOnWriteArrayList<>();

    public TrackSegment(String id, Node nodeA, Node nodeB, double length) {
        this.id = id;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.length = length;
        this.semaphoreAB = new Semaphore(1);
        this.semaphoreBA = new Semaphore(1);
        this.occupiedForward = false;
        this.occupiedBackward = false;
    }

    public String getId() { return id; }
    public Node getNodeA() { return nodeA; }
    public Node getNodeB() { return nodeB; }
    public double getLength() { return length; }
    
    /**
     * Returns the semaphore for the given direction.
     * @param forward true for A→B lane, false for B→A lane
     */
    public Semaphore getSemaphore(boolean forward) {
        return forward ? semaphoreAB : semaphoreBA;
    }

    public boolean isOccupied() { return occupiedForward || occupiedBackward; }
    public boolean isOccupiedForward() { return occupiedForward; }
    public boolean isOccupiedBackward() { return occupiedBackward; }

    /**
     * Sets occupation state for a specific directional lane.
     * Fires SegmentStateListener with the direction that changed.
     */
    public void setOccupied(boolean occupied, Train train, boolean forward) {
        if (forward) {
            this.occupiedForward = occupied;
            this.occupyingTrainForward = occupied ? train : null;
        } else {
            this.occupiedBackward = occupied;
            this.occupyingTrainBackward = occupied ? train : null;
        }
        notifyListeners(occupied, train, forward);
    }
    
    public Node getOppositeNode(Node node) {
        if (node.equals(nodeA)) return nodeB;
        if (node.equals(nodeB)) return nodeA;
        throw new IllegalArgumentException("Node is not connected to this segment");
    }

    // --- Observer pattern ---

    public void addListener(SegmentStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SegmentStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(boolean occupied, Train train, boolean forward) {
        for (SegmentStateListener listener : listeners) {
            listener.onSegmentStateChanged(this, occupied, train, forward);
        }
    }
}
