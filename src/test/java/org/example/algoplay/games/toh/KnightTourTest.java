package org.example.algoplay.games.toh;

import org.example.algoplay.models.BacktrackingKnightTour;
import org.example.algoplay.models.WarnsdorffKnightTour;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class KnightTourTest {

    @Test
    public void testWarnsdorffSmallBoardSolution() {
        WarnsdorffKnightTour warnsdorff = new WarnsdorffKnightTour(5);
        boolean solved = warnsdorff.solveKnightTour(0, 0);
        assertTrue(solved, "Warnsdorff algorithm should solve a 5x5 board");
        List<WarnsdorffKnightTour.Point> solution = warnsdorff.getSolutionPath();
        assertEquals(25, solution.size(), "Solution path should contain 25 positions for 5x5 board");
        validateKnightMoves(solution);
    }

    @Test
    public void testBacktrackingSmallBoardSolution() {
        BacktrackingKnightTour backtracking = new BacktrackingKnightTour(5);
        boolean solved = backtracking.solveKnightTour(0, 0);
        assertTrue(solved, "Backtracking algorithm should solve a 5x5 board");
        List<BacktrackingKnightTour.Point> solution = backtracking.getSolutionPath();
        assertEquals(25, solution.size(), "Solution path should contain 25 positions for 5x5 board");
        validateKnightMoves(solution);
    }


    @Test
    public void testWarnsdorffBoardCompleteness() {
        int size = 6;
        WarnsdorffKnightTour warnsdorff = new WarnsdorffKnightTour(size);
        warnsdorff.solveKnightTour(0, 0);
        int[][] board = warnsdorff.getBoard();
        boolean[] visited = new boolean[size * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                assertTrue(value >= 0 && value < size * size);
                assertFalse(visited[value]);
                visited[value] = true;
            }
        }
        for (boolean v : visited) {
            assertTrue(v);
        }
    }

    @Test
    public void testBacktrackingBoardCompleteness() {
        int size = 5;
        BacktrackingKnightTour backtracking = new BacktrackingKnightTour(size);
        backtracking.solveKnightTour(0, 0);
        int[][] board = backtracking.getBoard();
        boolean[] visited = new boolean[size * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                assertTrue(value >= 0 && value < size * size);
                assertFalse(visited[value]);
                visited[value] = true;
            }
        }
        for (boolean v : visited) {
            assertTrue(v);
        }
    }

    @Test
    public void testWarnsdorffGetValidMoves() {
        WarnsdorffKnightTour warnsdorff = new WarnsdorffKnightTour(8);
        List<WarnsdorffKnightTour.Point> existingMoves = new ArrayList<>();
        existingMoves.add(new WarnsdorffKnightTour.Point(0, 0));
        List<WarnsdorffKnightTour.Point> validMoves = warnsdorff.getValidMoves(0, 0, existingMoves);
        assertEquals(2, validMoves.size());
        existingMoves.add(new WarnsdorffKnightTour.Point(2, 1));
        validMoves = warnsdorff.getValidMoves(0, 0, existingMoves);
        assertEquals(1, validMoves.size());
        assertEquals(1, validMoves.get(0).x);
        assertEquals(2, validMoves.get(0).y);
    }

    @Test
    public void testBacktrackingGetValidMoves() {
        BacktrackingKnightTour backtracking = new BacktrackingKnightTour(8);
        List<BacktrackingKnightTour.Point> existingMoves = new ArrayList<>();
        existingMoves.add(new BacktrackingKnightTour.Point(0, 0));
        List<BacktrackingKnightTour.Point> validMoves = backtracking.getValidMoves(0, 0, existingMoves);
        assertEquals(2, validMoves.size());
        existingMoves.add(new BacktrackingKnightTour.Point(2, 1));
        validMoves = backtracking.getValidMoves(0, 0, existingMoves);
        assertEquals(1, validMoves.size());
        assertEquals(1, validMoves.get(0).x);
        assertEquals(2, validMoves.get(0).y);
    }

    @Test
    public void testWarnsdorffFindCompleteTourMove() {
        int size = 5;
        WarnsdorffKnightTour warnsdorff = new WarnsdorffKnightTour(size);
        List<WarnsdorffKnightTour.Point> existingMoves = new ArrayList<>();
        existingMoves.add(new WarnsdorffKnightTour.Point(0, 0));
        WarnsdorffKnightTour.Point nextMove = warnsdorff.findCompleteTourMove(existingMoves);
        assertNotNull(nextMove);
        existingMoves.add(nextMove);
        WarnsdorffKnightTour.Point secondMove = warnsdorff.findCompleteTourMove(existingMoves);
        assertNotNull(secondMove);
        assertTrue(isValidKnightMove(nextMove, secondMove));
    }

    @Test
    public void testBacktrackingFindCompleteTourMove() {
        int size = 5;
        BacktrackingKnightTour backtracking = new BacktrackingKnightTour(size);
        List<BacktrackingKnightTour.Point> existingMoves = new ArrayList<>();
        existingMoves.add(new BacktrackingKnightTour.Point(0, 0));
        BacktrackingKnightTour.Point nextMove = backtracking.findCompleteTourMove(0, 0, existingMoves);
        assertNotNull(nextMove);
        existingMoves.add(nextMove);
        BacktrackingKnightTour.Point secondMove = backtracking.findCompleteTourMove(nextMove.x, nextMove.y, existingMoves);
        assertNotNull(secondMove);
        assertTrue(isValidKnightMove(nextMove, secondMove));
    }

    @Test
    public void testPerformanceComparison() {
        int size = 6;
        long start = System.currentTimeMillis();
        new WarnsdorffKnightTour(size).solveKnightTour(0, 0);
        long warnsdorffTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        new BacktrackingKnightTour(size).solveKnightTour(0, 0);
        long backtrackingTime = System.currentTimeMillis() - start;

        System.out.println("Size " + size + " board:");
        System.out.println("Warnsdorff time: " + warnsdorffTime + "ms");
        System.out.println("Backtracking time: " + backtrackingTime + "ms");
    }

    @Test
    public void testImpossibleBoardForWarnsdorff() {
        WarnsdorffKnightTour warnsdorff = new WarnsdorffKnightTour(3);
        boolean solved = warnsdorff.solveKnightTour(0, 0);
        assertFalse(solved, "Warnsdorff should not solve 3x3 board");
    }

    @Test
    public void testImpossibleBoardForBacktracking() {
        BacktrackingKnightTour backtracking = new BacktrackingKnightTour(3);
        boolean solved = backtracking.solveKnightTour(0, 0);
        assertFalse(solved, "Backtracking should not solve 3x3 board");
    }

    // ------------------ Helper methods --------------------

    private <T> void validateKnightMoves(List<T> path) {
        for (int i = 1; i < path.size(); i++) {
            int x1, y1, x2, y2;

            if (path.get(i - 1) instanceof WarnsdorffKnightTour.Point) {
                WarnsdorffKnightTour.Point prev = (WarnsdorffKnightTour.Point) path.get(i - 1);
                WarnsdorffKnightTour.Point curr = (WarnsdorffKnightTour.Point) path.get(i);
                x1 = prev.x; y1 = prev.y;
                x2 = curr.x; y2 = curr.y;
            } else {
                BacktrackingKnightTour.Point prev = (BacktrackingKnightTour.Point) path.get(i - 1);
                BacktrackingKnightTour.Point curr = (BacktrackingKnightTour.Point) path.get(i);
                x1 = prev.x; y1 = prev.y;
                x2 = curr.x; y2 = curr.y;
            }

            assertTrue(isValidKnightMove(x1, y1, x2, y2),
                    "Invalid knight move from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
        }
    }

    private boolean isValidKnightMove(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
    }

    private boolean isValidKnightMove(Object from, Object to) {
        int x1, y1, x2, y2;
        if (from instanceof WarnsdorffKnightTour.Point) {
            x1 = ((WarnsdorffKnightTour.Point) from).x;
            y1 = ((WarnsdorffKnightTour.Point) from).y;
            x2 = ((WarnsdorffKnightTour.Point) to).x;
            y2 = ((WarnsdorffKnightTour.Point) to).y;
        } else {
            x1 = ((BacktrackingKnightTour.Point) from).x;
            y1 = ((BacktrackingKnightTour.Point) from).y;
            x2 = ((BacktrackingKnightTour.Point) to).x;
            y2 = ((BacktrackingKnightTour.Point) to).y;
        }
        return isValidKnightMove(x1, y1, x2, y2);
    }
}
