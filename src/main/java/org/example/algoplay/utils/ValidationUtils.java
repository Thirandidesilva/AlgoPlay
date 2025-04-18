package org.example.algoplay.utils;

import java.util.List;
import java.util.Stack;

/**
 * Utility class for validating Tower of Hanoi moves and other game validations
 */
public class ValidationUtils {

    /**
     * Validates a single Tower of Hanoi move
     * @param pegs current state of the pegs
     * @param sourceIndex source peg index
     * @param targetIndex target peg index
     * @return true if the move is valid, false otherwise
     */
    public static boolean isValidHanoiMove(List<Stack<Integer>> pegs, int sourceIndex, int targetIndex) {
        // Check if indices are valid
        if (sourceIndex < 0 || sourceIndex >= pegs.size() ||
                targetIndex < 0 || targetIndex >= pegs.size() ||
                sourceIndex == targetIndex) {
            return false;
        }

        // Check if source peg has disks
        Stack<Integer> sourcePeg = pegs.get(sourceIndex);
        if (sourcePeg.isEmpty()) {
            return false;
        }

        // Check if target peg is empty or if the top disk on source is smaller than the top disk on target
        Stack<Integer> targetPeg = pegs.get(targetIndex);
        if (targetPeg.isEmpty() || sourcePeg.peek() < targetPeg.peek()) {
            return true;
        }

        return false;
    }

    /**
     * Parses a move string in the format "Move disk X from peg A to peg B"
     * @param moveStr the move string to parse
     * @return int array with [diskSize, sourcePegIndex, targetPegIndex] or null if invalid format
     */
    public static int[] parseHanoiMove(String moveStr) {
        try {
            // Extract disk number
            int diskIndex = moveStr.indexOf("disk") + 5;
            int fromIndex = moveStr.indexOf("from");
            String diskStr = moveStr.substring(diskIndex, fromIndex).trim();
            int diskSize = Integer.parseInt(diskStr);

            // Extract source peg
            int pegIndex = moveStr.indexOf("peg", fromIndex) + 4;
            char sourcePeg = moveStr.charAt(pegIndex);
            int sourcePegIndex = sourcePeg - 'A';

            // Extract target peg
            int toIndex = moveStr.indexOf("to");
            int targetPegIndex = moveStr.charAt(moveStr.indexOf("peg", toIndex) + 4) - 'A';

            return new int[] {diskSize, sourcePegIndex, targetPegIndex};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates a sequence of Tower of Hanoi moves
     * @param numDisks number of disks
     * @param movesList list of move strings
     * @param isFourPeg whether it's a 4-peg game (true) or 3-peg game (false)
     * @return true if the moves solve the puzzle, false otherwise
     */
    public static boolean validateHanoiSolution(int numDisks, List<String> movesList, boolean isFourPeg) {
        int pegCount = isFourPeg ? 4 : 3;
        List<Stack<Integer>> pegs = initializePegs(numDisks, pegCount);

        for (String moveStr : movesList) {
            int[] move = parseHanoiMove(moveStr);
            if (move == null) {
                return false;
            }

            int diskSize = move[0];
            int sourcePegIndex = move[1];
            int targetPegIndex = move[2];

            // Check if the move is valid
            if (!isValidHanoiMove(pegs, sourcePegIndex, targetPegIndex)) {
                return false;
            }

            // Execute the move
            int disk = pegs.get(sourcePegIndex).pop();
            pegs.get(targetPegIndex).push(disk);

            // Check if disk size matches
            if (disk != diskSize) {
                return false;
            }
        }

        // Check if all disks are on the destination peg
        return pegs.get(pegCount - 1).size() == numDisks;
    }

    private static List<Stack<Integer>> initializePegs(int numDisks, int pegCount) {
        List<Stack<Integer>> pegs = new java.util.ArrayList<>();

        // Create the pegs
        for (int i = 0; i < pegCount; i++) {
            pegs.add(new Stack<>());
        }

        // Initialize the first peg with disks
        Stack<Integer> sourcePeg = pegs.get(0);
        for (int i = numDisks; i > 0; i--) {
            sourcePeg.push(i);
        }

        return pegs;
    }
}