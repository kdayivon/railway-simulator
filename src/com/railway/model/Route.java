package com.railway.model;

import java.util.List;

public record Route(List<TrackSegment> segments, Station origin, Station destination) {
}
