package com.railway.config;

import com.railway.model.*;
import com.railway.util.RouteBuilder;
import java.util.List;

public class TrainConfig {

    public static void setupTrains(RailwayNetwork network) {
          Train IC_GBL = new Train("IC_GBL", TrainType.EXPRESS, RouteBuilder.build(network, "Ghent", "Brussels", "Liège"));
          Train IC_NBAG = new Train("IC_NBAG", TrainType.EXPRESS, RouteBuilder.build(network, "Namur", "Brussels", "Antwerp", "Ghent"));
          Train L_AG = new Train("L_AG", TrainType.LOCAL, RouteBuilder.build(network, "Antwerp", "Ghent"), true);
          Train L_GA = new Train("L_GA", TrainType.LOCAL, RouteBuilder.build(network, "Ghent", "Antwerp"), true);
          Train IC_LN = new Train("IC_LN", TrainType.EXPRESS, RouteBuilder.build(network, "Liège", "Namur"), true);
          Train IC_LN2 = new Train("IC_LN2", TrainType.EXPRESS, RouteBuilder.build(network, "Liège", "Namur"));
          Train IC_NL = new Train("IC_NL", TrainType.EXPRESS, RouteBuilder.build(network, "Namur", "Liège"));
          Train IC_NB = new Train("IC_NB", TrainType.EXPRESS, RouteBuilder.build(network, "Namur", "Brussels"));
          Train IC_MA = new Train("IC_MA", TrainType.EXPRESS, RouteBuilder.build(network, "Mons", "Antwerp"));

        network.trains.addAll(List.of(IC_GBL, IC_NBAG, L_AG, L_GA, IC_LN, IC_LN2, IC_NL, IC_NB, IC_MA));
    }
}
