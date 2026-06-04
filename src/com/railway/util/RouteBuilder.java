package com.railway.util;

import com.railway.model.*;
import java.util.*;

public class RouteBuilder {
    public static Route build(RailwayNetwork network, String... stationNames) {
        if (stationNames.length < 2) {
            throw new IllegalArgumentException("Need at least 2 stations to build a route");
        }
        
        List<TrackSegment> fullPath = new ArrayList<>();
        Station origin = findStationByName(network, stationNames[0]);
        Station destination = null;
        
        for (int i = 0; i < stationNames.length - 1; i++) {
            String startName = stationNames[i];
            String endName = stationNames[i+1];
            
            Node startNode = findStationByName(network, startName);
            Node endNode = findStationByName(network, endName);
            destination = (Station) endNode;
            
            List<TrackSegment> segmentPath = findShortestPath(startNode, endNode);
            fullPath.addAll(segmentPath);
        }
        
        return new Route(fullPath, origin, destination);
    }
    
    private static Station findStationByName(RailwayNetwork network, String name) {
        return network.stations.stream()
            .filter(s -> s.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + name));
    }
    
    private static List<TrackSegment> findShortestPath(Node start, Node end) {
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        Map<Node, Double> distTo = new HashMap<>();
        Map<Node, TrackSegment> edgeTo = new HashMap<>();
        
        pq.add(new NodeDistance(start, 0.0));
        distTo.put(start, 0.0);
        
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Node u = current.node;
            
            if (u.equals(end)) {
                break;
            }
            
            if (current.distance > distTo.getOrDefault(u, Double.POSITIVE_INFINITY)) {
                continue;
            }
            
            for (TrackSegment segment : u.getConnectedSegments()) {
                Node v = segment.getOppositeNode(u);
                double weight = segment.getLength();
                double newDist = distTo.get(u) + weight;
                
                if (newDist < distTo.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    distTo.put(v, newDist);
                    edgeTo.put(v, segment);
                    pq.add(new NodeDistance(v, newDist));
                }
            }
        }
        
        if (!edgeTo.containsKey(end)) {
            throw new RuntimeException("No path found between " + start.getId() + " and " + end.getId());
        }
        
        List<TrackSegment> path = new ArrayList<>();
        Node current = end;
        while (!current.equals(start)) {
            TrackSegment seg = edgeTo.get(current);
            path.add(0, seg);
            current = seg.getOppositeNode(current);
        }
        
        return path;
    }


}
