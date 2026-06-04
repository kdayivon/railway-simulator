package com.railway.engine;

import com.railway.model.DirectedSegment;
import com.railway.model.Route;
import com.railway.model.TrackSegment;
import com.railway.model.Train;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class StationMaster {

    private static class TrainRequest {
        Train train;
        List<DirectedSegment> requestedPath;
        CompletableFuture<Route> future;

        TrainRequest(Train train, List<DirectedSegment> requestedPath, CompletableFuture<Route> future) {
            this.train = train;
            this.requestedPath = requestedPath;
            this.future = future;
        }
    }

    private final ConcurrentLinkedQueue<TrainRequest> requestQueue = new ConcurrentLinkedQueue<>();
    private final Semaphore signalSemaphore = new Semaphore(0);

    private final Thread masterThread;

    public StationMaster() {
        masterThread = Thread.ofVirtual().start(this::loop);
    }

    // 1. Pre-registration: Train asks for a route leg with direction info
    public CompletableFuture<Route> preRegister(Train train, List<DirectedSegment> requestedPath) {
        CompletableFuture<Route> future = new CompletableFuture<>();
        requestQueue.add(new TrainRequest(train, requestedPath, future));
        signalSemaphore.release();
        return future;
    }

    // 4. Movement & Release: Train calls this when its tail clears a segment
    public void onSegmentCleared(TrackSegment segment, boolean forward) {
        segment.setOccupied(false, null, forward);
        segment.getSemaphore(forward).release();
        signalSemaphore.release();
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                signalSemaphore.acquire();
                processQueue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // 3. Route Allocation
    private void processQueue() {
        for (TrainRequest request : requestQueue) {
            if (tryAllocateRoute(request)) {
                requestQueue.remove(request);
            }
        }
    }

    private boolean tryAllocateRoute(TrainRequest request) {
        List<DirectedSegment> path = request.requestedPath;
        
        for (DirectedSegment ds : path) {
            if (ds.segment().getSemaphore(ds.forward()).availablePermits() == 0) {
                return false;
            }
        }

        boolean success = true;
        int acquiredCount = 0;
        for (DirectedSegment ds : path) {
            if (ds.segment().getSemaphore(ds.forward()).tryAcquire()) {
                acquiredCount++;
            } else {
                success = false;
                break;
            }
        }

        if (success) {
            List<TrackSegment> segments = path.stream().map(DirectedSegment::segment).toList();
            Route lockedRoute = new Route(segments, null, null);
            request.future.complete(lockedRoute);
            return true;
        } else {
            for (int i = 0; i < acquiredCount; i++) {
                DirectedSegment ds = path.get(i);
                ds.segment().getSemaphore(ds.forward()).release();
            }
            return false;
        }
    }
}
