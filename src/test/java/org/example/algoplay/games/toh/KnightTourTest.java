package org.example.algoplay.games.toh;

import org.example.algoplay.models.BacktrackingKnightTour;
import org.example.algoplay.models.WarnsdorffKnightTour;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnightTourTest {

    @Test
    void testBacktrackingKnightTourSolve() {
        BacktrackingKnightTour knightTour = new BacktrackingKnightTour(5); // 5x5 board
        boolean solved = knightTour.solveKnightTour(0, 0);

        assertTrue(solved, "BacktrackingKnightTour should solve 5x5 starting at (0,0)");

        int[][] board = knightTour.getBoard();
        assertEquals(0, board[0][0], "Start position should be 0");
        assertAllSquaresVisited(board);
    }

    @Test
    void testWarnsdorffKnightTourSolve() {
        WarnsdorffKnightTour knightTour = new WarnsdorffKnightTour(8); // 8x8 board
        boolean solved = knightTour.solveKnightTour(0, 0);

        assertTrue(solved, "WarnsdorffKnightTour should solve 8x8 starting at (0,0)");

        int[][] board = knightTour.getBoard();
        assertEquals(0, board[0][0], "Start position should be 0");
        assertAllSquaresVisited(board);
    }

    @Test
    void testBacktrackingKnightTourGetSolutionPath() {
        BacktrackingKnightTour knightTour = new BacktrackingKnightTour(5);
        knightTour.solveKnightTour(0, 0);

        List<BacktrackingKnightTour.Point> path = knightTour.getSolutionPath();
        assertEquals(25, path.size(), "Path should contain 25 moves on 5x5 board");
        assertNotNull(path.get(0), "First point should not be null");
    }

    @Test
    void testWarnsdorffKnightTourGetSolutionPath() {
        WarnsdorffKnightTour knightTour = new WarnsdorffKnightTour(6);
        knightTour.solveKnightTour(0, 0);

        List<WarnsdorffKnightTour.Point> path = knightTour.getSolutionPath();
        assertEquals(36, path.size(), "Path should contain 36 moves on 6x6 board");
        assertNotNull(path.get(0), "First point should not be null");
    }

    private void assertAllSquaresVisited(int[][] board) {
        int size = board.length;
        boolean[] visited = new boolean[size * size];
        for (int[] row : board) {
            for (int cell : row) {
                assertTrue(cell >= 0 && cell < size * size, "Each cell must be within correct move range");
                visited[cell] = true;
            }
        }
        for (boolean wasVisited : visited) {
            assertTrue(wasVisited, "Every move number must appear exactly once");
        }
    }
}


