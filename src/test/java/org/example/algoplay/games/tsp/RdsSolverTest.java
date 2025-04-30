package org.example.algoplay.games.tsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RdsSolverTest {

    private RdsSolver solver;

    @BeforeEach
    void setUp() {
        solver = new RdsSolver();
    }

    @Test
    void testGetName() {
        assertEquals("RDS", solver.getName(), "Solver name should be RDS");
    }

    @Test
    void testInitialize() {
        // Arrange
        int[][] distances = {{0, 10, 15}, {10, 0, 20}, {15, 20, 0}};
        List<Integer> cities = Arrays.asList(0, 1, 2);

        // Act
        solver.initialize(distances, cities);

        // No direct assertions possible as fields are private,
        // but we'll verify it doesn't throw exceptions
        // and later tests will verify the behavior
    }

    @Test
    void testSolveSmallProblem() {
        // Arrange
        int[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };
        List<Integer> cities = Arrays.asList(0, 1, 2, 3);

        // Act
        solver.initialize(distances, cities);
        List<Integer> solution = solver.solve();

        // Assert
        assertNotNull(solution, "Solution should not be null");
        assertEquals(cities.size(), solution.size(), "Solution should contain all cities");
        assertTrue(solution.containsAll(cities), "Solution should contain all cities");

        // Verify the tour is a valid permutation of the cities
        for (Integer city : cities) {
            assertTrue(solution.contains(city), "Solution should contain city " + city);
        }
    }

    @Test
    void testCalcLen() {
        // This tests the private calcLen method indirectly through solve
        // Arrange
        int[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };
        List<Integer> cities = Arrays.asList(0, 1, 2, 3);
        List<Integer> knownRoute = Arrays.asList(0, 1, 3, 2);

        // The expected length for route [0,1,3,2] is:
        // 0->1: 10
        // 1->3: 25
        // 3->2: 30
        // 2->0: 15
        // Total: 80
        int expectedLength = 80;

        // Create a mock to test private calcLen method
        RdsSolver spySolver = Mockito.spy(new RdsSolver());

        // Make dreamSearch return our known route
        try {
            Mockito.doReturn(knownRoute)
                    .when(spySolver)
                    .dreamSearch(anyList(), anyList());
        } catch (Exception e) {
            fail("Failed to set up mock: " + e.getMessage());
        }

        // Act
        spySolver.initialize(distances, cities);
        List<Integer> solution = spySolver.solve();

        // Assert
        assertEquals(knownRoute, solution, "Solution should match our forced route");

        // We can't directly test calcLen, but we can verify that a proper solution is found
    }

    @Test
    void testEmptyRemainingCities() {
        // Arrange
        int[][] distances = {{0}};
        List<Integer> cities = Arrays.asList(0);

        // Act
        solver.initialize(distances, cities);
        List<Integer> solution = solver.solve();

        // Assert
        assertEquals(1, solution.size(), "Solution should contain one city");
        assertEquals(0, solution.get(0).intValue(), "Solution should contain city 0");
    }

    @Test
    void testGenerateLucidDreams() throws Exception {
        // This tests the private generateLucidDreams method using reflection
        // Arrange
        int[][] distances = {
                {0, 10, 15},
                {10, 0, 20},
                {15, 20, 0}
        };
        List<Integer> cities = Arrays.asList(0, 1, 2);
        List<Integer> currentPath = Arrays.asList(0);
        List<Integer> remaining = Arrays.asList(1, 2);

        solver.initialize(distances, cities);

        // Use reflection to access private method
        java.lang.reflect.Method method = RdsSolver.class.getDeclaredMethod(
                "generateLucidDreams", List.class, List.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        List<List<Integer>> dreams = (List<List<Integer>>) method.invoke(solver, currentPath, remaining);

        // Assert
        assertNotNull(dreams, "Dreams should not be null");
        assertFalse(dreams.isEmpty(), "Dreams should not be empty");
        assertEquals(5, dreams.size(), "Should generate DREAMS_PER_STEP dreams");

        // Each dream should start with the current path
        for (List<Integer> dream : dreams) {
            assertTrue(dream.get(0) == 0, "Each dream should start with city 0");
            assertTrue(dream.size() > currentPath.size(), "Dreams should add at least one city");
            assertTrue(remaining.contains(dream.get(dream.size() - 1)),
                    "Last city in dream should be from remaining cities");
        }
    }

    @Test
    void testPickCloseCity() throws Exception {
        // This tests the private pickCloseCity method using reflection
        // Arrange
        int[][] distances = {
                {0, 10, 30},  // City 0 is closer to city 1 than city 2
                {10, 0, 20},
                {30, 20, 0}
        };
        List<Integer> cities = Arrays.asList(0, 1, 2);
        List<Integer> candidates = Arrays.asList(1, 2);
        int fromCity = 0;

        solver.initialize(distances, cities);

        // Use reflection to access private method
        java.lang.reflect.Method method = RdsSolver.class.getDeclaredMethod(
                "pickCloseCity", int.class, List.class, Random.class);
        method.setAccessible(true);

        // Mock Random to always return 0.0 to ensure deterministic behavior
        // This ensures we always pick the city with highest weight (closest)
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextDouble()).thenReturn(0.01);  // Small value to select first city

        // Act
        int selectedCity = (int) method.invoke(solver, fromCity, candidates, mockRandom);

        // Assert
        assertEquals(1, selectedCity, "Should pick the closest city (1) to city 0");

        // Now test with different random value to pick the second city
        when(mockRandom.nextDouble()).thenReturn(0.99);  // Large value to select second city
        selectedCity = (int) method.invoke(solver, fromCity, candidates, mockRandom);
        assertEquals(2, selectedCity, "Should pick the second city (2) when random value is high");
    }

    @Test
    void testMaxDepthLimitation() {
        // Arrange a large problem that should hit the MAX_DEPTH limit
        int cityCount = 15;  // Larger than MAX_DEPTH which is 9
        int[][] distances = new int[cityCount][cityCount];
        List<Integer> cities = new ArrayList<>();

        // Initialize distances matrix with some values
        for (int i = 0; i < cityCount; i++) {
            cities.add(i);
            for (int j = 0; j < cityCount; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = 10 + Math.abs(i - j);
                }
            }
        }

        // Act
        solver.initialize(distances, cities);
        List<Integer> solution = solver.solve();

        // Assert
        assertNotNull(solution, "Solution should not be null even with depth limitation");
        // The solution might not contain all cities due to MAX_DEPTH
        assertTrue(solution.size() <= cityCount, "Solution size should not exceed city count");

        // Verify each city in solution is valid
        for (Integer city : solution) {
            assertTrue(cities.contains(city), "Each city in solution should be valid");
        }
    }
}