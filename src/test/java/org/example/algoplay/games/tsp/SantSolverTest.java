package org.example.algoplay.games.tsp;

import org.example.algoplay.games.tsp.SantSolver;
import org.example.algoplay.games.tsp.TspSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SantSolverTest {

    private SantSolver solver;
    private int[][] distanceMatrix;
    private List<Integer> cities;

    @BeforeEach
    void setUp() {
        solver = new SantSolver();

        // Create a simple distance matrix for testing
        // This represents a small graph with 4 cities
        distanceMatrix = new int[][] {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        cities = Arrays.asList(0, 1, 2, 3);
    }

    @Test
    void testInitialization() {
        solver.initialize(distanceMatrix, cities);
        // Verify that initialization doesn't throw exceptions
        assertNotNull(solver);
        assertEquals("Sant Colony Optimization", solver.getName());
    }

    @Test
    void testSolve() {
        solver.initialize(distanceMatrix, cities);
        solver.setIterations(20); // Reduce iterations for faster testing

        List<Integer> tour = solver.solve();

        // Verify the tour contains all cities
        assertNotNull(tour);
        assertEquals(cities.size(), tour.size());
        for (Integer city : cities) {
            assertTrue(tour.contains(city));
        }
    }

    @Test
    void testSolveWithOptimalSolution() {
        // For this simple distance matrix, the optimal tour is 0-1-3-2-0 or reverse
        // with length = 10 + 25 + 30 + 15 = 80
        solver.initialize(distanceMatrix, cities);

        // Set high iterations to ensure convergence
        solver.setIterations(100);

        // Run the solver multiple times to account for randomness
        int bestLength = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            List<Integer> tour = solver.solve();
            int length = calculateTourLength(tour, distanceMatrix);
            if (length < bestLength) {
                bestLength = length;
            }
        }

        // The algorithm is stochastic, so we just check that it found a reasonably good solution
        // rather than asserting the exact optimal solution
        assertTrue(bestLength <= 90, "Expected a solution with length <= 90, but got: " + bestLength);
    }

    @Test
    void testSolveAndReturnTopPaths() {
        solver.initialize(distanceMatrix, cities);
        solver.setIterations(20);

        int numPaths = 3;
        List<List<Integer>> topPaths = solver.solveAndReturnTopPaths(numPaths);

        // Verify we got the requested number of paths
        assertNotNull(topPaths);
        assertTrue(topPaths.size() <= numPaths);

        // Verify each path contains all cities
        for (List<Integer> path : topPaths) {
            assertEquals(cities.size(), path.size());
            for (Integer city : cities) {
                assertTrue(path.contains(city));
            }
        }

        // Verify paths are sorted by length (if we have multiple paths)
        if (topPaths.size() > 1) {
            int firstLength = calculateTourLength(topPaths.get(0), distanceMatrix);
            int secondLength = calculateTourLength(topPaths.get(1), distanceMatrix);
            assertTrue(firstLength <= secondLength,
                    "Expected first path to be shorter than or equal to second path");
        }
    }

    @Test
    void testParameterSetting() {
        solver.initialize(distanceMatrix, cities);

        // Test that we can change parameters without errors
        solver.setIterations(50);
        List<Integer> tour = solver.solve();
        assertNotNull(tour);
    }

    @Test
    void testLargerProblem() {
        // Test with a larger problem to ensure scalability
        int size = 10;
        int[][] largeMatrix = new int[size][size];
        List<Integer> largeCities = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        // Fill with some test data (Euclidean distances in a grid)
        int[][] points = new int[][] {
                {0, 0}, {1, 5}, {2, 3}, {5, 2}, {6, 7},
                {7, 1}, {8, 4}, {9, 8}, {4, 9}, {3, 6}
        };

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    largeMatrix[i][j] = 0;
                } else {
                    // Calculate Euclidean distance
                    int dx = points[i][0] - points[j][0];
                    int dy = points[i][1] - points[j][1];
                    largeMatrix[i][j] = (int) Math.sqrt(dx*dx + dy*dy);
                }
            }
        }

        solver.initialize(largeMatrix, largeCities);
        solver.setIterations(30); // Fewer iterations for test speed

        List<Integer> tour = solver.solve();

        // Verify the tour contains all cities
        assertNotNull(tour);
        assertEquals(largeCities.size(), tour.size());
        for (Integer city : largeCities) {
            assertTrue(tour.contains(city));
        }
    }

    // Helper method to calculate tour length
    private int calculateTourLength(List<Integer> tour, int[][] distances) {
        int length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            length += distances[tour.get(i)][tour.get(i + 1)];
        }
        // Add the distance from last to first city to complete the tour
        length += distances[tour.get(tour.size() - 1)][tour.get(0)];
        return length;
    }
}