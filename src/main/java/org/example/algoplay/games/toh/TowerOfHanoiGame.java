package org.example.algoplay.games.toh;

import org.example.algoplay.utils.TimeTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TowerOfHanoiGame {
    private int numDisks;
    private List<Stack<Integer>> pegs; // 3 or 4 pegs
    private List<String> moveHistory;
    private boolean isFourPegMode;

    // Algorithm solvers
    private RecursiveHanoiSolver recursiveSolver;
    private IterativeHanoiSolver iterativeSolver;
    private FourPegHanoiSolver fourPegSolver;

    // Performance tracking
    private long recursiveTime;
    private long iterativeTime;
    private long fourPegTime;

    public TowerOfHanoiGame(int numDisks, boolean isFourPegMode) {
        this.numDisks = numDisks;
        this.isFourPegMode = isFourPegMode;
        this.moveHistory = new ArrayList<>();

        initializePegs();

        this.recursiveSolver = new RecursiveHanoiSolver();
        this.iterativeSolver = new IterativeHanoiSolver();
        if (isFourPegMode) {
            this.fourPegSolver = new FourPegHanoiSolver();
        }
    }

    private void initializePegs() {
        pegs = new ArrayList<>();

        // Create the pegs (3 or 4)
        int pegCount = isFourPegMode ? 4 : 3;
        for (int i = 0; i < pegCount; i++) {
            pegs.add(new Stack<>());
        }

        // Initialize the first peg with disks
        Stack<Integer> sourcePeg = pegs.get(0);
        for (int i = numDisks; i > 0; i--) {
            sourcePeg.push(i);
        }
    }

    public String solveWithRecursiveAlgorithm() {
        // Reset the game
        initializePegs();
        moveHistory.clear();

        // Time the recursive solution
        TimeTracker timer = new TimeTracker();
        timer.start();

        List<String> moves = recursiveSolver.solve(numDisks, 0, 2, 1);

        recursiveTime = timer.stop();
        moveHistory.addAll(moves);

        return generateMovesSequence(moves);
    }

    public String solveWithIterativeAlgorithm() {
        // Reset the game
        initializePegs();
        moveHistory.clear();

        // Time the iterative solution
        TimeTracker timer = new TimeTracker();
        timer.start();

        List<String> moves = iterativeSolver.solve(numDisks);

        iterativeTime = timer.stop();
        moveHistory.addAll(moves);

        return generateMovesSequence(moves);
    }

    public String solveWithFourPegAlgorithm() {
        if (!isFourPegMode) {
            throw new IllegalStateException("Game is not in four peg mode");
        }

        // Reset the game
        initializePegs();
        moveHistory.clear();

        // Time the four peg solution
        TimeTracker timer = new TimeTracker();
        timer.start();

        List<String> moves = fourPegSolver.solve(numDisks);

        fourPegTime = timer.stop();
        moveHistory.addAll(moves);

        return generateMovesSequence(moves);
    }

    private String generateMovesSequence(List<String> moves) {
        StringBuilder sb = new StringBuilder();
        for (String move : moves) {
            sb.append(move).append("\n");
        }
        return sb.toString();
    }

    public int getOptimalMovesCount() {
        // For standard 3-peg Tower of Hanoi, the optimal move count is 2^n - 1
        if (!isFourPegMode) {
            return (int) Math.pow(2, numDisks) - 1;
        } else {
            // For 4-peg Tower of Hanoi (Frame-Stewart algorithm)
            // This is an approximation as the exact formula is complex
            return fourPegSolver.getOptimalMoveCount(numDisks);
        }
    }

    public boolean validateUserSolution(List<String> userMoves) {
        return userMoves.size() <= getOptimalMovesCount();
    }

    // Getters for performance metrics
    public long getRecursiveTime() { return recursiveTime; }
    public long getIterativeTime() { return iterativeTime; }
    public long getFourPegTime() { return fourPegTime; }
    public int getNumDisks() { return numDisks; }
    public List<String> getMoveHistory() { return moveHistory; }

    public List<Stack<Integer>> getPegs() {
        return pegs;
    }
}
