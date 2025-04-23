package org.example.algoplay.games.tsp;

import java.util.List;

public interface TspSolver {
    void initialize(int[][] distanceMatrix, List<Integer> cities);
    List<Integer> solve();
    String getName();
}
