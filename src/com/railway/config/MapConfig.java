package com.railway.config;

import com.railway.model.*;
import java.util.List;

public class MapConfig {
    public static RailwayNetwork createMap() {
        RailwayNetwork network = new RailwayNetwork();

        Station brussels = new Station("ST_BRU", "Brussels", 400, 250, 5);
        Station ghent = new Station("ST_GHE", "Ghent", 200, 100, 5);
        Station antwerp = new Station("ST_ANT", "Antwerp", 400, 100, 5);
        Station namur = new Station("ST_NAM", "Namur", 300, 500, 5);
        Station liege = new Station("ST_LIE", "Liège", 700, 400, 5);
        Station mons = new Station("ST_MONS", "Mons", 100, 300, 5);

        network.stations.addAll(List.of(brussels, ghent, antwerp, namur, liege, mons));

        Switch swNamurBru = new Switch("SW_NAM_BRU", 300, 400);
        network.switches.add(swNamurBru);

        // Track segments
        TrackSegment tGhentAntwerp = new TrackSegment("T_GHE_ANT", ghent, antwerp, 200);
        TrackSegment tGhentBrussels = new TrackSegment("T_GHE_BRU", ghent, brussels, 283);
        TrackSegment tAntwerpBrussels = new TrackSegment("T_ANT_BRU", antwerp, brussels, 200);
        TrackSegment tNamurLiege = new TrackSegment("T_NAM_LIE", namur, liege, 300);
        TrackSegment tLiegeBrussels = new TrackSegment("T_LIE_BRU", liege, brussels, 283);
        TrackSegment tSwitchMons = new TrackSegment("T_SW_MONS", swNamurBru, mons, 300);
        
        TrackSegment tNamurSwitch = new TrackSegment("T_NAM_SW", namur, swNamurBru, 100);
        TrackSegment tSwitchBrussels = new TrackSegment("T_SW_BRU", swNamurBru, brussels, 141);

        ghent.addSegment(tGhentAntwerp); antwerp.addSegment(tGhentAntwerp);
        ghent.addSegment(tGhentBrussels); brussels.addSegment(tGhentBrussels);
        antwerp.addSegment(tAntwerpBrussels); brussels.addSegment(tAntwerpBrussels);
        namur.addSegment(tNamurLiege); liege.addSegment(tNamurLiege);
        liege.addSegment(tLiegeBrussels); brussels.addSegment(tLiegeBrussels);
        namur.addSegment(tNamurSwitch); swNamurBru.addSegment(tNamurSwitch);
        swNamurBru.addSegment(tSwitchBrussels); brussels.addSegment(tSwitchBrussels);
        swNamurBru.addSegment(tSwitchMons); mons.addSegment(tSwitchMons);

        network.tracks.addAll(List.of(
            tGhentAntwerp, tGhentBrussels, tAntwerpBrussels, 
            tNamurLiege, tLiegeBrussels, tNamurSwitch, tSwitchBrussels, tSwitchMons
        ));

        return network;
    }
}
