package com.railway.model;

/**
 * Pairs a TrackSegment with the direction of travel.
 * Used by StationMaster to acquire the correct directional semaphore.
 */
public record DirectedSegment(TrackSegment segment, boolean forward) {}
