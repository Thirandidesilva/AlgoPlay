package org.example.algoplay.games.toh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class TowerOfHanoiTest {

    private TowerOfHanoiGame threePegGame;
    private TowerOfHanoiGame fourPegGame;
    private RecursiveHanoiSolver recursiveSolver;
    private IterativeHanoiSolver iterativeSolver;
    private FourPegHanoiSolver fourPegSolver;

    @BeforeEach
    void setUp() {
        // Initialize games with 3 disks for simplicity in tests
        threePegGame = new TowerOfHanoiGame(3, false);
        fourPegGame = new TowerOfHanoiGame(3, true);

        recursiveSolver = new RecursiveHanoiSolver();
        iterativeSolver = new IterativeHanoiSolver();
        fourPegSolver = new FourPegHanoiSolver();
    }

    @Test
    @DisplayName("Test initial game state")
    void testInitialGameState() {
        // Check that the first peg has all disks
        List<Stack<Integer>> threePegs = threePegGame.getPegs();
        assertEquals(3, threePegs.size());
        assertEquals(3, threePegs.get(0).size());
        assertEquals(0, threePegs.get(1).size());
        assertEquals(0, threePegs.get(2).size());

        // Check disk order (largest at bottom)
        assertEquals(3, threePegs.get(0).get(0));
        assertEquals(2, threePegs.get(0).get(1));
        assertEquals(1, threePegs.get(0).get(2));

        // Check four peg mode initialization
        List<Stack<Integer>> fourPegs = fourPegGame.getPegs();
        assertEquals(4, fourPegs.size());
    }

    @Test
    @DisplayName("Test optimal move count calculation")
    void testOptimalMoveCount() {
        // For 3 pegs with n disks, optimal move count is 2^n - 1
        assertEquals(7, threePegGame.getOptimalMovesCount()); // 2^3 - 1 = 7

        // Create games with different disk counts
        TowerOfHanoiGame game4Disks = new TowerOfHanoiGame(4, false);
        assertEquals(15, game4Disks.getOptimalMovesCount()); // 2^4 - 1 = 15

        // Four peg mode uses a different algorithm
        // Just verify it returns a valid number (should be less than 3-peg solution)
        int fourPegMoves = fourPegGame.getOptimalMovesCount();
        assertTrue(fourPegMoves > 0);
        assertTrue(fourPegMoves <= 7); // Should be <= the 3-peg solution
    }

    @Test
    @DisplayName("Test recursive solver generates correct number of moves")
    void testRecursiveSolver() {
        List<String> moves = recursiveSolver.solve(3, 0, 2, 1);
        assertEquals(7, moves.size()); // 2^3 - 1 = 7 moves

        // Check first and last moves
        assertTrue(moves.get(0).contains("disk 1"));
        assertTrue(moves.get(moves.size() - 1).contains("disk 1"));
    }

    @Test
    @DisplayName("Test iterative solver generates correct number of moves")
    void testIterativeSolver() {
        List<String> moves = iterativeSolver.solve(3);
        assertEquals(7, moves.size()); // 2^3 - 1 = 7 moves
    }

    @Test
    @DisplayName("Test four peg solver generates valid moves")
    void testFourPegSolver() {
        List<String> moves = fourPegSolver.solve(3);

        // Verify it generated some moves
        assertTrue(moves.size() > 0);

        // The Four-Peg solution should produce fewer moves than the 3-peg solution
        // for sufficiently large n (but may be same for n=3)
        assertTrue(moves.size() <= 7);
    }

    @Test
    @DisplayName("Test solving with recursive algorithm")
    void testSolveWithRecursiveAlgorithm() {
        String result = threePegGame.solveWithRecursiveAlgorithm();

        // Check that moves were recorded
        assertFalse(result.isEmpty());
        assertEquals(7, threePegGame.getMoveHistory().size());

        // Check that execution time was recorded
        assertTrue(threePegGame.getRecursiveTime() >= 0);
    }

    @Test
    @DisplayName("Test solving with iterative algorithm")
    void testSolveWithIterativeAlgorithm() {
        String result = threePegGame.solveWithIterativeAlgorithm();

        // Check that moves were recorded
        assertFalse(result.isEmpty());
        assertEquals(7, threePegGame.getMoveHistory().size());

        // Check that execution time was recorded
        assertTrue(threePegGame.getIterativeTime() >= 0);
    }

    @Test
    @DisplayName("Test solving with four peg algorithm")
    void testSolveWithFourPegAlgorithm() {
        // Should throw exception if not in four peg mode
        assertThrows(IllegalStateException.class, () -> threePegGame.solveWithFourPegAlgorithm());

        // Should work in four peg mode
        String result = fourPegGame.solveWithFourPegAlgorithm();

        // Check that moves were recorded
        assertFalse(result.isEmpty());
        assertTrue(fourPegGame.getMoveHistory().size() > 0);

        // Check that execution time was recorded
        assertTrue(fourPegGame.getFourPegTime() >= 0);
    }

    @Test
    @DisplayName("Test finding optimal k for Frame-Stewart algorithm")
    void testFindOptimalK() {
        // Create an accessible method to test
        int k5 = fourPegSolver.findOptimalK(5);
        assertTrue(k5 > 0);
        assertTrue(k5 < 5); // k should be less than n

        int k10 = fourPegSolver.findOptimalK(10);
        assertTrue(k10 > 0);
        assertTrue(k10 < 10);

        // For n=4, k should be 2 (sqrt(4))
        int k4 = fourPegSolver.findOptimalK(4);
        assertEquals(2, k4);
    }

    @Test
    @DisplayName("Test validate user solution")
    void testValidateUserSolution() {
        // Empty solution is always valid
        assertTrue(threePegGame.validateUserSolution(List.of()));

        // Optimal solution should be valid
        List<String> optimalMoves = recursiveSolver.solve(3, 0, 2, 1);
        assertTrue(threePegGame.validateUserSolution(optimalMoves));

        // Creating too many moves
        List<String> tooManyMoves = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tooManyMoves.add("Move disk 1 from peg A to peg B");
        }
        assertFalse(threePegGame.validateUserSolution(tooManyMoves));
    }
}