package org.example.algoplay.games.toh;

import java.util.List;
import java.util.Stack;
import org.example.algoplay.utils.TimeTracker;

/**
 * Manual test class for Tower of Hanoi game
 */
public class TowerOfHanoiManualTest {

    public static void main(String[] args) {
        System.out.println("==== Tower of Hanoi Game Test ====\n");

        // Test with different disk counts and modes
        testBasicFunctionality(3, false); // 3 disks, 3 pegs
        testBasicFunctionality(4, false); // 4 disks, 3 pegs
        testBasicFunctionality(3, true);  // 3 disks, 4 pegs

        // Test algorithm performance comparison
        compareAlgorithmPerformance();

        // Test edge cases
        testEdgeCases();

        System.out.println("\n==== All tests completed! ====");
    }

    private static void testBasicFunctionality(int diskCount, boolean fourPegMode) {
        String pegMode = fourPegMode ? "4 pegs" : "3 pegs";
        System.out.println("\n----- Testing " + diskCount + " disks with " + pegMode + " -----");

        // Create a new game instance
        TowerOfHanoiGame game = new TowerOfHanoiGame(diskCount, fourPegMode);

        // Verify initial state
        System.out.println("Initial state:");
        System.out.println("- Disk count: " + game.getNumDisks());
        System.out.println("- Peg count: " + game.getPegs().size());
        System.out.println("- Optimal moves: " + game.getOptimalMovesCount());
        printPegStates(game);

        // Test recursive algorithm
        System.out.println("\nSolving with recursive algorithm...");
        String recursiveMoves = game.solveWithRecursiveAlgorithm();
        System.out.println("- Moves: " + game.getMoveHistory().size());
        System.out.println("- Time: " + game.getRecursiveTime() + " ms");
        verifyGameSolution(game);

        // Test iterative algorithm
        System.out.println("\nSolving with iterative algorithm...");
        String iterativeMoves = game.solveWithIterativeAlgorithm();
        System.out.println("- Moves: " + game.getMoveHistory().size());
        System.out.println("- Time: " + game.getIterativeTime() + " ms");
        verifyGameSolution(game);

        // Test four peg algorithm if in four peg mode
        if (fourPegMode) {
            System.out.println("\nSolving with four peg algorithm...");
            String fourPegMoves = game.solveWithFourPegAlgorithm();
            System.out.println("- Moves: " + game.getMoveHistory().size());
            System.out.println("- Time: " + game.getFourPegTime() + " ms");
            verifyGameSolution(game);
        }
    }

    private static void compareAlgorithmPerformance() {
        System.out.println("\n----- Algorithm Performance Comparison -----");

        int[] diskCounts = {3, 5, 7, 10};

        System.out.println("Disk Count | Recursive (ms) | Iterative (ms) | Four Peg (ms)");
        System.out.println("--------------------------------------------------------");

        for (int diskCount : diskCounts) {
            // Test with 3 pegs
            TowerOfHanoiGame game3Peg = new TowerOfHanoiGame(diskCount, false);
            game3Peg.solveWithRecursiveAlgorithm();
            long recursiveTime = game3Peg.getRecursiveTime();

            game3Peg.solveWithIterativeAlgorithm();
            long iterativeTime = game3Peg.getIterativeTime();

            // Test with 4 pegs
            TowerOfHanoiGame game4Peg = new TowerOfHanoiGame(diskCount, true);
            game4Peg.solveWithFourPegAlgorithm();
            long fourPegTime = game4Peg.getFourPegTime();

            System.out.printf("%-10d | %-14d | %-14d | %-11d\n",
                    diskCount, recursiveTime, iterativeTime, fourPegTime);
        }
    }

    private static void testEdgeCases() {
        System.out.println("\n----- Testing Edge Cases -----");

        // Test with minimum disk count
        System.out.println("\nTesting with 1 disk:");
        TowerOfHanoiGame minGame = new TowerOfHanoiGame(1, false);
        minGame.solveWithRecursiveAlgorithm();
        System.out.println("- Moves: " + minGame.getMoveHistory().size() + " (expected: 1)");
        verifyGameSolution(minGame);

        // Test switching between 3-peg and 4-peg modes
        System.out.println("\nTesting mode switching:");
        TowerOfHanoiGame game = new TowerOfHanoiGame(3, false);
        game.solveWithRecursiveAlgorithm();
        int threePegMoves = game.getMoveHistory().size();

        game = new TowerOfHanoiGame(3, true);
        game.solveWithFourPegAlgorithm();
        int fourPegMoves = game.getMoveHistory().size();

        System.out.println("- 3-peg moves: " + threePegMoves);
        System.out.println("- 4-peg moves: " + fourPegMoves);
        System.out.println("- Four peg algorithm is more efficient: " + (fourPegMoves < threePegMoves));

        // Test manual moves
        System.out.println("\nTesting manual moves:");
        testManualMoves();
    }

    private static void testManualMoves() {
        // Create a new game with 3 disks and 3 pegs
        TowerOfHanoiGame game = new TowerOfHanoiGame(3, false);

        // Get pegs for direct manipulation (simulating UI actions)
        List<Stack<Integer>> pegs = game.getPegs();

        // Manually make the moves for a 3-disk solution
        // Move pattern for 3 disks: 0→2, 0→1, 2→1, 0→2, 1→0, 1→2, 0→2
        System.out.println("Initial state:");
        printPegStates(game);

        // Move 1: disk 1 from peg A to peg C
        if (!pegs.get(0).isEmpty() && pegs.get(0).peek() == 1) {
            int disk = pegs.get(0).pop();
            pegs.get(2).push(disk);
            game.getMoveHistory().add("Move disk 1 from peg A to peg C");
            System.out.println("Move 1: disk 1 from A to C");
            printPegStates(game);
        }

        // Move 2: disk 2 from peg A to peg B
        if (!pegs.get(0).isEmpty() && pegs.get(0).peek() == 2) {
            int disk = pegs.get(0).pop();
            pegs.get(1).push(disk);
            game.getMoveHistory().add("Move disk 2 from peg A to peg B");
            System.out.println("Move 2: disk 2 from A to B");
            printPegStates(game);
        }

        // Move 3: disk 1 from peg C to peg B
        if (!pegs.get(2).isEmpty() && pegs.get(2).peek() == 1) {
            int disk = pegs.get(2).pop();
            pegs.get(1).push(disk);
            game.getMoveHistory().add("Move disk 1 from peg C to peg B");
            System.out.println("Move 3: disk 1 from C to B");
            printPegStates(game);
        }

        // Move 4: disk 3 from peg A to peg C
        if (!pegs.get(0).isEmpty() && pegs.get(0).peek() == 3) {
            int disk = pegs.get(0).pop();
            pegs.get(2).push(disk);
            game.getMoveHistory().add("Move disk 3 from peg A to peg C");
            System.out.println("Move 4: disk 3 from A to C");
            printPegStates(game);
        }

        // Move 5: disk 1 from peg B to peg A
        if (!pegs.get(1).isEmpty() && pegs.get(1).peek() == 1) {
            int disk = pegs.get(1).pop();
            pegs.get(0).push(disk);
            game.getMoveHistory().add("Move disk 1 from peg B to peg A");
            System.out.println("Move 5: disk 1 from B to A");
            printPegStates(game);
        }

        // Move 6: disk 2 from peg B to peg C
        if (!pegs.get(1).isEmpty() && pegs.get(1).peek() == 2) {
            int disk = pegs.get(1).pop();
            pegs.get(2).push(disk);
            game.getMoveHistory().add("Move disk 2 from peg B to peg C");
            System.out.println("Move 6: disk 2 from B to C");
            printPegStates(game);
        }

        // Move 7: disk 1 from peg A to peg C
        if (!pegs.get(0).isEmpty() && pegs.get(0).peek() == 1) {
            int disk = pegs.get(0).pop();
            pegs.get(2).push(disk);
            game.getMoveHistory().add("Move disk 1 from peg A to peg C");
            System.out.println("Move 7: disk 1 from A to C");
            printPegStates(game);
        }

        // Verify solution
        System.out.println("Manual solution completed with " + game.getMoveHistory().size() + " moves");
        verifyGameSolution(game);
    }

    private static void printPegStates(TowerOfHanoiGame game) {
        List<Stack<Integer>> pegs = game.getPegs();

        for (int i = 0; i < pegs.size(); i++) {
            Stack<Integer> peg = pegs.get(i);
            System.out.print("Peg " + (char)('A' + i) + ": ");

            if (peg.isEmpty()) {
                System.out.println("(empty)");
            } else {
                // Clone the stack to avoid modifying the game state
                @SuppressWarnings("unchecked")
                Stack<Integer> tempStack = (Stack<Integer>) peg.clone();
                Integer[] disks = new Integer[tempStack.size()];

                // Pop disks from temporary stack to get them in reverse order
                for (int j = tempStack.size() - 1; j >= 0; j--) {
                    disks[j] = tempStack.pop();
                }

                // Print disks from bottom to top
                for (int disk : disks) {
                    System.out.print(disk + " ");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    private static void verifyGameSolution(TowerOfHanoiGame game) {
        List<Stack<Integer>> pegs = game.getPegs();
        int targetPegIndex = game.getPegs().size() - 1; // Last peg
        Stack<Integer> targetPeg = pegs.get(targetPegIndex);

        boolean isSolved = targetPeg.size() == game.getNumDisks();

        // Additionally, check that disks are in correct order
        if (isSolved) {
            boolean correctOrder = true;
            @SuppressWarnings("unchecked")
            Stack<Integer> tempStack = (Stack<Integer>) targetPeg.clone();
            int prevDisk = -1;

            while (!tempStack.isEmpty()) {
                int disk = tempStack.pop();
                if (prevDisk != -1 && disk > prevDisk) {
                    correctOrder = false;
                    break;
                }
                prevDisk = disk;
            }

            isSolved = isSolved && correctOrder;
        }

        System.out.println("Solution valid: " + isSolved);
    }
}
