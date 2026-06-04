package com.railway.model;

/**
 * Observer interface for TrackSegment occupation state changes.
 * Decouples the model layer from the UI layer
 */
public interface SegmentStateListener {
    /**
     * Fired when a segment's occupation state changes.
     *
     * @param segment  the segment whose state changed
     * @param occupied true if a train entered, false if cleared
     * @param train    the occupying train (non-null when occupied, null when cleared)
     * @param forward  true if the A→B lane changed, false if the B→A lane changed
     */
    void onSegmentStateChanged(TrackSegment segment, boolean occupied, Train train, boolean forward);
}
