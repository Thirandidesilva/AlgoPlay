package org.example.algoplay.games.tsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SbeeSolverTest {

    private SbeeSolver solver;
    private int[][] distanceMatrix;
    private List<Integer> cities;

    @BeforeEach
    void setUp() {
        solver = new SbeeSolver();

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
        assertEquals("Bee Colony Optimization", solver.getName());
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
    void testParameterSetting() {
        solver.initialize(distanceMatrix, cities);

        // Test that we can change parameters without errors
        solver.setIterations(50);
        List<Integer> tour = solver.solve();
        assertNotNull(tour);
    }

    @Test
    void testInitialPopulation() {
        solver.initialize(distanceMatrix, cities);

        // Create an initial population with a known good solution
        List<List<Integer>> initialPopulation = new ArrayList<>();
        // Add a good path: 0-1-3-2 (which is close to optimal for our test data)
        initialPopulation.add(Arrays.asList(0, 1, 3, 2));
        // Add some random paths
        initialPopulation.add(Arrays.asList(0, 2, 1, 3));
        initialPopulation.add(Arrays.asList(3, 1, 0, 2));

        solver.setInitialPopulation(initialPopulation);
        solver.setIterations(10); // Fewer iterations as we have a good start

        List<Integer> tour = solver.solve();

        // Verify we get a valid tour
        assertNotNull(tour);
        assertEquals(cities.size(), tour.size());

        // Check the tour length - should be good since we started with a good solution
        int tourLength = calculateTourLength(tour, distanceMatrix);
        assertTrue(tourLength <= 90, "Expected a good solution with initial population");
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

    @Test
    void testConsistencyWithSameParameters() {
        solver.initialize(distanceMatrix, cities);
        solver.setIterations(50);

        // Run multiple times and ensure we always get valid tours
        for (int i = 0; i < 3; i++) {
            List<Integer> tour = solver.solve();
            assertNotNull(tour);
            assertEquals(cities.size(), tour.size());

            // Each city should appear exactly once
            for (Integer city : cities) {
                int count = 0;
                for (Integer tourCity : tour) {
                    if (tourCity.equals(city)) {
                        count++;
                    }
                }
                assertEquals(1, count, "City " + city + " should appear exactly once");
            }
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