package org.example.algoplay.games.tsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HybridSolverTest {

    private HybridSolver hybridSolver;
    private int[][] distanceMatrix;
    private List<Integer> cities;

    @BeforeEach
    void setUp() {
        hybridSolver = new HybridSolver();

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
        hybridSolver.initialize(distanceMatrix, cities);
        // Verify that initialization doesn't throw exceptions
        assertNotNull(hybridSolver);
        assertEquals("Hybrid Ant + Bee Optimization", hybridSolver.getName());
    }

    @Test
    void testSolve() {
        hybridSolver.initialize(distanceMatrix, cities);
        // Use smaller iteration counts for testing
        hybridSolver.setAntIterations(10);
        hybridSolver.setBeeIterations(10);

        List<Integer> tour = hybridSolver.solve();

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
        hybridSolver.initialize(distanceMatrix, cities);

        // Set reasonable iterations to get a good solution
        hybridSolver.setAntIterations(30);
        hybridSolver.setBeeIterations(30);

        List<Integer> tour = hybridSolver.solve();
        int tourLength = calculateTourLength(tour, distanceMatrix);

        // The algorithm combines two stochastic methods, but should find good solutions
        assertTrue(tourLength <= 90, "Expected a solution with length <= 90, but got: " + tourLength);
    }

    @Test
    void testParameterSetting() {
        hybridSolver.initialize(distanceMatrix, cities);

        // Test various parameter combinations
        hybridSolver.setAntIterations(20);
        hybridSolver.setBeeIterations(30);
        hybridSolver.setNumElitePathsToPass(3);

        List<Integer> tour = hybridSolver.solve();

        // Just verify we get a valid solution without exceptions
        assertNotNull(tour);
        assertEquals(cities.size(), tour.size());
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

        hybridSolver.initialize(largeMatrix, largeCities);

        // Reduce iterations for test speed
        hybridSolver.setAntIterations(10);
        hybridSolver.setBeeIterations(10);

        List<Integer> tour = hybridSolver.solve();

        // Verify the tour contains all cities
        assertNotNull(tour);
        assertEquals(largeCities.size(), tour.size());
        for (Integer city : largeCities) {
            assertTrue(tour.contains(city));
        }
    }

    @Test
    void testPathTransferBetweenSolvers() {
        hybridSolver.initialize(distanceMatrix, cities);

        // Use only 1 elite path to simplify verification
        hybridSolver.setNumElitePathsToPass(1);

        // Use significant ant iterations but minimal bee iterations
        // This helps test that Bee solver is actually using paths from Ant solver
        hybridSolver.setAntIterations(50);
        hybridSolver.setBeeIterations(1);

        List<Integer> tour = hybridSolver.solve();

        // Verify we get a valid tour
        assertNotNull(tour);
        assertEquals(cities.size(), tour.size());

        // Since we're using many ant iterations but only 1 bee iteration,
        // we're essentially testing that the ant solution is being passed to the bee solver
        int tourLength = calculateTourLength(tour, distanceMatrix);
        assertTrue(tourLength <= 90, "Expected the hybrid approach to find a good solution");
    }

    @Test
    void testComparisonWithIndividualSolvers() {
        // This test compares the hybrid with standalone ant and bee solvers
        hybridSolver.initialize(distanceMatrix, cities);
        hybridSolver.setAntIterations(20);
        hybridSolver.setBeeIterations(20);

        // Create standalone solvers for comparison
        SantSolver santOnly = new SantSolver();
        santOnly.initialize(distanceMatrix, cities);
        santOnly.setIterations(20);

        SbeeSolver sbeeOnly = new SbeeSolver();
        sbeeOnly.initialize(distanceMatrix, cities);
        sbeeOnly.setIterations(20);

        // Run all solvers
        List<Integer> hybridTour = hybridSolver.solve();
        List<Integer> antTour = santOnly.solve();
        List<Integer> beeTour = sbeeOnly.solve();

        // Calculate tour lengths
        int hybridLength = calculateTourLength(hybridTour, distanceMatrix);
        int antLength = calculateTourLength(antTour, distanceMatrix);
        int beeLength = calculateTourLength(beeTour, distanceMatrix);

        // For curiosity's sake, print the results
        System.out.println("Hybrid solver length: " + hybridLength);
        System.out.println("Ant solver length: " + antLength);
        System.out.println("Bee solver length: " + beeLength);

        // Verify all produced valid tours
        assertEquals(cities.size(), hybridTour.size());
        assertEquals(cities.size(), antTour.size());
        assertEquals(cities.size(), beeTour.size());
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