package com.railway.ui;

import com.railway.model.*;
import com.railway.model.RailwayNetwork;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders the railway network on a JavaFX Canvas.
 * Implements SegmentStateListener to receive occupation events
 * from the model layer
 */
public class MapRenderer implements SegmentStateListener {
    private final Canvas canvas;
    private final RailwayNetwork network;

    private static final double TRACK_OFFSET = 4.0;
    private static final Color TRACK_FREE = Color.web("#34a853");       // Green
    private static final Color TRACK_OCCUPIED = Color.web("#ea4335");   // Red
    private static final Color[] TRAIN_COLORS = {
        Color.web("#E63946"),  // Red
        Color.web("#457B9D"),  // Blue
        Color.web("#2A9D8F"),  // Teal
        Color.web("#E9C46A"),  // Yellow
        Color.web("#F4A261"),  // Brown
        Color.web("#264653"),  // Dark Blue
    };

    private final ConcurrentHashMap<String, Train> forwardOccupied = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Train> backwardOccupied = new ConcurrentHashMap<>();

    public MapRenderer(Canvas canvas, RailwayNetwork network) {
        this.canvas = canvas;
        this.network = network;
        for (TrackSegment track : network.tracks) {
            track.addListener(this);
        }
    }

    @Override
    public void onSegmentStateChanged(TrackSegment segment, boolean occupied, Train train, boolean forward) {
        ConcurrentHashMap<String, Train> map = forward ? forwardOccupied : backwardOccupied;
        if (occupied && train != null) {
            map.put(segment.getId(), train);
        } else {
            map.remove(segment.getId());
        }
    }


    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw Tracks (parallel inbound/outbound lines)
        gc.setLineWidth(3);
        for (TrackSegment track : network.tracks) {
            double x1 = track.getNodeA().getX();
            double y1 = track.getNodeA().getY();
            double x2 = track.getNodeB().getX();
            double y2 = track.getNodeB().getY();

            // Compute perpendicular unit vector
            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) continue;
            double perpX = -dy / len;
            double perpY = dx / len;

            // A→B lane (offset to the right of the direction vector)
            double ax1 = x1 + perpX * TRACK_OFFSET;
            double ay1 = y1 + perpY * TRACK_OFFSET;
            double ax2 = x2 + perpX * TRACK_OFFSET;
            double ay2 = y2 + perpY * TRACK_OFFSET;

            // B→A lane (offset to the left)
            double bx1 = x1 - perpX * TRACK_OFFSET;
            double by1 = y1 - perpY * TRACK_OFFSET;
            double bx2 = x2 - perpX * TRACK_OFFSET;
            double by2 = y2 - perpY * TRACK_OFFSET;

            // Per-lane coloring from decoupled observer state
            Color forwardColor = forwardOccupied.containsKey(track.getId()) ? TRACK_OCCUPIED : TRACK_FREE;
            Color backwardColor = backwardOccupied.containsKey(track.getId()) ? TRACK_OCCUPIED : TRACK_FREE;

            gc.setStroke(forwardColor);
            gc.strokeLine(ax1, ay1, ax2, ay2);
            gc.setStroke(backwardColor);
            gc.strokeLine(bx1, by1, bx2, by2);
        }

        // Draw Switches
        for (Switch sw : network.switches) {
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            double r = 6;
            gc.fillOval(sw.getX() - r, sw.getY() - r, r * 2, r * 2);
            gc.strokeOval(sw.getX() - r, sw.getY() - r, r * 2, r * 2);
        }

        // Draw Stations
        for (Station st : network.stations) {
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            double w = 20;
            double h = 20;
            gc.fillRect(st.getX() - w/2, st.getY() - h/2, w, h);
            gc.strokeRect(st.getX() - w/2, st.getY() - h/2, w, h);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText(st.getName(), st.getX() + 15, st.getY() + 5);
        }
        
        // Draw Trains
        for (int i = 0; i < network.trains.size(); i++) {
            Train train = network.trains.get(i);
            if (train.getCurrentSegment() != null) {
                TrackSegment seg = train.getCurrentSegment();
                double progress = Math.min(train.getPositionOnSegment() / seg.getLength(), 1.0);
                
                double x1 = seg.getNodeA().getX();
                double y1 = seg.getNodeA().getY();
                double x2 = seg.getNodeB().getX();
                double y2 = seg.getNodeB().getY();
                
                // Compute perpendicular for offset
                double dx = x2 - x1;
                double dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) continue;
                double perpX = -dy / len;
                double perpY = dx / len;
                
                // Offset direction: forward (A→B) uses +offset, backward (B→A) uses -offset
                double offsetSign = train.isForwardOnSegment() ? 1.0 : -1.0;

                // Interpolate position along the center line
                double centerX, centerY;
                if (train.isForwardOnSegment()) {
                    centerX = x1 + dx * progress;
                    centerY = y1 + dy * progress;
                } else {
                    // Moving B→A: progress 0 is at B, progress 1 is at A
                    centerX = x2 - dx * progress;
                    centerY = y2 - dy * progress;
                }
                
                // Apply perpendicular offset to place on the correct parallel track
                double tx = centerX + perpX * TRACK_OFFSET * offsetSign;
                double ty = centerY + perpY * TRACK_OFFSET * offsetSign;
                
                Color color = TRAIN_COLORS[i % TRAIN_COLORS.length];
                gc.setFill(color);
                double r = 6;
                gc.fillOval(tx - r, ty - r, r * 2, r * 2);
                
                // Draw train ID label
                gc.setFill(color);
                gc.setFont(Font.font("Arial", 10));
                gc.fillText(train.getId(), tx + 8, ty - 4);
            }
        }
    }
}
