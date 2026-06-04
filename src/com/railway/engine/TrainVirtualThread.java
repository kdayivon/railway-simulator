package com.railway.engine;

import com.railway.model.DirectedSegment;
import com.railway.model.Node;
import com.railway.model.Route;
import com.railway.model.Station;
import com.railway.model.TrackSegment;
import com.railway.model.Train;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TrainVirtualThread implements Runnable {

    private final Train train;
    private final StationMaster stationMaster;

    public TrainVirtualThread(Train train, StationMaster stationMaster) {
        this.train = train;
        this.stationMaster = stationMaster;
    }

    @Override
    public void run() {
        try {
            Route route = train.getRoute();
            if (route == null || route.segments() == null || route.segments().isEmpty()) return;

            Node origin = route.origin();
            List<List<DirectedSegment>> legs = splitIntoLegs(route.segments(), origin);

            do {
                for (List<DirectedSegment> leg : legs) {
                    // 1. Pre-register for this station-to-station leg only
                    CompletableFuture<Route> future = stationMaster.preRegister(train, leg);

                    // 2. Staging — block the virtual thread until this leg is allocated
                    future.join();

                    // 3. Movement through the leg
                    double maxSpeed = train.getType().getMaxSpeed();

                    for (DirectedSegment ds : leg) {
                        TrackSegment segment = ds.segment();
                        boolean forward = ds.forward();

                        train.setForwardOnSegment(forward);
                        train.setCurrentSegment(segment);
                        train.setPositionOnSegment(0);
                        train.setCurrentVelocity(maxSpeed);

                        // Mark segment as occupied on the correct lane — fires SegmentStateListener
                        segment.setOccupied(true, train, forward);

                        long lastTime = System.nanoTime();

                        while (train.getPositionOnSegment() < segment.getLength()) {
                            long currentTime = System.nanoTime();
                            double deltaTimeSec = (currentTime - lastTime) / 1_000_000_000.0;
                            lastTime = currentTime;

                            // d = v * dt
                            double step = train.getCurrentVelocity() * deltaTimeSec;
                            train.setPositionOnSegment(train.getPositionOnSegment() + step);

                            // Yield or sleep slightly to not max out CPU in tight loop
                            Thread.sleep(10);
                        }

                        // 4. Release the directional lane — fires SegmentStateListener
                        stationMaster.onSegmentCleared(segment, forward);
                    }
                }

                // Reached final destination
                train.setCurrentVelocity(0);
                train.setCurrentSegment(null);

                if (train.isLoopRoute()) {
                    Thread.sleep(1000);
                }

            } while (train.isLoopRoute());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Splits a flat list of segments into station-to-station legs,
     * computing the direction of travel for each segment.
     * A leg ends when the exit node of a segment is a Station.
     */
    private List<List<DirectedSegment>> splitIntoLegs(List<TrackSegment> segments, Node origin) {
        List<List<DirectedSegment>> legs = new ArrayList<>();
        List<DirectedSegment> currentLeg = new ArrayList<>();
        Node current = origin;

        for (TrackSegment segment : segments) {
            boolean forward = current.getId().equals(segment.getNodeA().getId());
            currentLeg.add(new DirectedSegment(segment, forward));

            Node exit = segment.getOppositeNode(current);

            if (exit instanceof Station) {
                // This segment ends at a station — end of this leg
                legs.add(currentLeg);
                currentLeg = new ArrayList<>();
            }

            current = exit;
        }

        // Add remaining segments if the route doesn't end at a station
        if (!currentLeg.isEmpty()) {
            legs.add(currentLeg);
        }

        return legs;
    }
}
