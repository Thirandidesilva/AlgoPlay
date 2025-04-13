package org.example.algoplay.games.toh;

import java.util.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Improved Tower of Hanoi Game Implementation
 * Features:
 * - Enhanced recursive solution with better error handling
 * - Improved iterative solution with explicit state tracking
 * - Frame-Stewart algorithm for 4-peg version
 * - Better visualization and user interaction
 */
public class TohAlgorithm {
    private static final String DB_URL = "jdbc:sqlite:hanoi_game.db";
    private static Scanner scanner = new Scanner(System.in);

    // Game pegs representation for visualization
    private static Map<Character, Stack<Integer>> pegs = new HashMap<>();

    public static void main(String[] args) {
        try {
            initializeDatabase();

            boolean running = true;
            while (running) {
                displayMenu();
                int choice = getUserChoice();

                switch (choice) {
                    case 1:
                        playClassicGame(3); // 3-peg version
                        break;
                    case 2:
                        playClassicGame(4); // 4-peg version
                        break;
                    case 3:
                        viewHighScores();
                        break;
                    case 4:
                        compareAlgorithms();
                        break;
                    case 5:
                        showInstructions();
                        break;
                    case 6:
                        running = false;
                        System.out.println("Thank you for playing Tower of Hanoi! Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void displayMenu() {
        System.out.println("\n🏰 TOWER OF HANOI 🏰");
        System.out.println("====================");
        System.out.println("1. Play Classic Game (3 pegs)");
        System.out.println("2. Play Extended Game (4 pegs)");
        System.out.println("3. View High Scores");
        System.out.println("4. Compare Algorithms");
        System.out.println("5. Instructions");
        System.out.println("6. Exit");
        System.out.print("Enter your choice (1-6): ");
    }

    private static int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Invalid choice
        }
    }

    /**
     * Play Tower of Hanoi game with the specified number of pegs
     * @param pegCount Number of pegs (3 for classic, 4 for extended version)
     */
    private static void playClassicGame(int pegCount) {
        try {
            // 1. Get player name with validation
            String playerName = "";
            while (playerName.trim().isEmpty()) {
                System.out.print("\nEnter your name: ");
                playerName = scanner.nextLine().trim();
                if (playerName.isEmpty()) {
                    System.out.println("Name cannot be empty. Please try again.");
                }
            }

            // 2. Randomly select number of disks (5-10)
            Random random = new Random();
            int diskCount = random.nextInt(1) + 3;

            System.out.println("\n🎮 NEW GAME - " + diskCount + " DISKS on " + pegCount + " pegs");

            // Initialize pegs
            initializeTowers(diskCount, pegCount);

            // Display initial state
            System.out.println("Initial tower state:");
            displayTowers(pegCount);

            // 3. Ask for approach: manual moves or sequence
            System.out.println("\nHow would you like to solve this puzzle?");
            System.out.println("1. Enter complete solution sequence");
            System.out.println("2. Make moves one by one");
            System.out.print("Enter your choice (1-2): ");

            int approachChoice = getUserChoice();

            if (approachChoice == 1) {
                // Solution sequence approach
                solutionSequenceApproach(playerName, diskCount, pegCount);
            } else if (approachChoice == 2) {
                // Interactive approach
                interactiveApproach(playerName, diskCount, pegCount);
            } else {
                System.out.println("Invalid choice. Defaulting to solution sequence approach.");
                solutionSequenceApproach(playerName, diskCount, pegCount);
            }

        } catch (Exception e) {
            System.out.println("Error during game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Approach where user provides the complete solution sequence upfront
     */
    private static void solutionSequenceApproach(String playerName, int diskCount, int pegCount) {
        try {
            // Ask for minimum number of moves
            System.out.print("\nEnter the minimum number of moves you think are needed: ");
            int userMoveCount = -1;
            while (userMoveCount < 0) {
                try {
                    userMoveCount = Integer.parseInt(scanner.nextLine());
                    if (userMoveCount < 0) {
                        System.out.print("Number of moves must be positive. Try again: ");
                    }
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a valid number: ");
                }
            }

            // Ask for sequence of moves
            System.out.println("\nEnter your sequence of moves (format: A->C,B->C,...):");
            String userMoveLine = scanner.nextLine().trim();

            // Process user's solution
            List<String> userMoves = new ArrayList<>();
            if (!userMoveLine.isEmpty()) {
                userMoves = Arrays.asList(userMoveLine.split(","));
                for (int i = 0; i < userMoves.size(); i++) {
                    userMoves.set(i, userMoves.get(i).trim());
                }
            }

            // Generate solutions with timing
            List<String> solution;
            long recursiveTimeMs = 0;
            long iterativeTimeMs = 0;
            long frameStewartTimeMs = 0;

            if (pegCount == 3) {
                // 3-peg solutions
                long recursiveStartTime = System.nanoTime();
                List<String> recursiveSolution = solveRecursive(diskCount, 'A', 'B', 'C');
                long recursiveEndTime = System.nanoTime();
                recursiveTimeMs = (recursiveEndTime - recursiveStartTime) / 1_000_000;

                long iterativeStartTime = System.nanoTime();
                List<String> iterativeSolution = solveIterative(diskCount, 'A', 'B', 'C');
                long iterativeEndTime = System.nanoTime();
                iterativeTimeMs = (iterativeEndTime - iterativeStartTime) / 1_000_000;

                // Verify that both solutions are equivalent
                if (!recursiveSolution.equals(iterativeSolution)) {
                    System.out.println("WARNING: Recursive and iterative solutions differ!");
                }

                solution = recursiveSolution;
            } else {
                // 4-peg solutions (Frame-Stewart)
                long frameStewartStartTime = System.nanoTime();
                solution = solveFrameStewart(diskCount, 'A', 'B', 'C', 'D');
                long frameStewartEndTime = System.nanoTime();
                frameStewartTimeMs = (frameStewartEndTime - frameStewartStartTime) / 1_000_000;
            }

            // Validate user's solution
            boolean correctCount = userMoves.size() == solution.size();
            boolean correctSequence = userMoves.equals(solution);

            // Display results
            if (correctCount && correctSequence) {
                System.out.println("\n✅ CORRECT! You solved the Tower of Hanoi puzzle!");

                if (pegCount == 3) {
                    System.out.println("Time taken by recursive solution: " + recursiveTimeMs + " ms");
                    System.out.println("Time taken by iterative solution: " + iterativeTimeMs + " ms");
                } else {
                    System.out.println("Time taken by Frame-Stewart algorithm: " + frameStewartTimeMs + " ms");
                }

                // Save result to database
                saveResult(playerName, diskCount, pegCount, true, userMoveLine,
                        recursiveTimeMs, iterativeTimeMs, frameStewartTimeMs);
                System.out.println("Your score has been saved!");
            } else {
                System.out.println("\n❌ INCORRECT SOLUTION");
                if (!correctCount) {
                    System.out.println("Expected " + solution.size() + " moves, but you entered " + userMoves.size());
                }
                if (!correctSequence) {
                    System.out.println("The move sequence is not correct.");
                }

                // Display correct solution
                System.out.println("\nCorrect solution:");
                for (int i = 0; i < Math.min(solution.size(), 20); i++) {
                    System.out.println((i+1) + ". " + solution.get(i));
                }

                if (solution.size() > 20) {
                    System.out.println("... and " + (solution.size() - 20) + " more moves");
                }

                // Save failed attempt to database
                saveResult(playerName, diskCount, pegCount, false, userMoveLine,
                        recursiveTimeMs, iterativeTimeMs, frameStewartTimeMs);
            }

            // Show final state
            System.out.println("\nFinal tower state (if correct):");
            // Reset and simulate the solution for visualization
            initializeTowers(diskCount, pegCount);
            simulateSolution(solution);
            displayTowers(pegCount);

        } catch (Exception e) {
            System.out.println("Error processing solution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Approach where user makes moves one by one interactively
     */
    private static void interactiveApproach(String playerName, int diskCount, int pegCount) {
        try {
            List<String> userMoves = new ArrayList<>();
            boolean gameContinue = true;
            boolean gameSuccess = false;

            char[] availablePegs = new char[pegCount];
            for (int i = 0; i < pegCount; i++) {
                availablePegs[i] = (char)('A' + i);
            }

            // Generate correct solution for comparison
            List<String> correctSolution;
            long recursiveTimeMs = 0;
            long iterativeTimeMs = 0;
            long frameStewartTimeMs = 0;

            if (pegCount == 3) {
                // 3-peg solutions
                long recursiveStartTime = System.nanoTime();
                List<String> recursiveSolution = solveRecursive(diskCount, 'A', 'B', 'C');
                long recursiveEndTime = System.nanoTime();
                recursiveTimeMs = (recursiveEndTime - recursiveStartTime) / 1_000_000;

                long iterativeStartTime = System.nanoTime();
                List<String> iterativeSolution = solveIterative(diskCount, 'A', 'B', 'C');
                long iterativeEndTime = System.nanoTime();
                iterativeTimeMs = (iterativeEndTime - iterativeStartTime) / 1_000_000;

                correctSolution = recursiveSolution;
            } else {
                // 4-peg solutions (Frame-Stewart)
                long frameStewartStartTime = System.nanoTime();
                correctSolution = solveFrameStewart(diskCount, 'A', 'B', 'C', 'D');
                long frameStewartEndTime = System.nanoTime();
                frameStewartTimeMs = (frameStewartEndTime - frameStewartStartTime) / 1_000_000;
            }

            System.out.println("\nMake your moves one by one. Enter 'quit' to exit the game.");
            System.out.println("Format: sourcePeg->destinationPeg (e.g., A->C)");

            while (gameContinue) {
                // Display current state
                displayTowers(pegCount);

                // Check if game is complete
                if (isGameComplete(diskCount, pegCount)) {
                    gameSuccess = true;
                    gameContinue = false;
                    continue;
                }

                // Get next move
                System.out.print("\nEnter your move: ");
                String move = scanner.nextLine().trim();

                if (move.equalsIgnoreCase("quit")) {
                    gameContinue = false;
                    continue;
                }

                // Validate move format
                if (!move.matches("[A-" + (char)('A' + pegCount - 1) + "]->[A-" + (char)('A' + pegCount - 1) + "]")) {
                    System.out.println("Invalid move format. Use sourcePeg->destinationPeg (e.g., A->C)");
                    continue;
                }

                // Parse move
                char fromPeg = move.charAt(0);
                char toPeg = move.charAt(3);

                // Validate pegs exist
                boolean validPegs = false;
                for (char peg : availablePegs) {
                    if (peg == fromPeg || peg == toPeg) {
                        validPegs = true;
                    }
                }
                if (!validPegs) {
                    System.out.println("Invalid peg. Available pegs: " + Arrays.toString(availablePegs));
                    continue;
                }

                // Check if move is valid
                if (!isValidMove(fromPeg, toPeg)) {
                    System.out.println("Invalid move. You cannot place a larger disk on a smaller one.");
                    continue;
                }

                // Execute the move
                executeMove(fromPeg, toPeg);
                userMoves.add(move);

                System.out.println("Move executed: " + move);
            }

            // Game ended - check results
            if (gameSuccess) {
                System.out.println("\n✅ CONGRATULATIONS! You solved the Tower of Hanoi puzzle!");
                System.out.println("You used " + userMoves.size() + " moves. Minimum possible: " + correctSolution.size());

                if (pegCount == 3) {
                    System.out.println("Time taken by recursive solution: " + recursiveTimeMs + " ms");
                    System.out.println("Time taken by iterative solution: " + iterativeTimeMs + " ms");
                } else {
                    System.out.println("Time taken by Frame-Stewart algorithm: " + frameStewartTimeMs + " ms");
                }

                // Build move sequence string
                StringBuilder movesStr = new StringBuilder();
                for (int i = 0; i < userMoves.size(); i++) {
                    movesStr.append(userMoves.get(i));
                    if (i < userMoves.size() - 1) {
                        movesStr.append(",");
                    }
                }

                // Save result to database
                saveResult(playerName, diskCount, pegCount, true, movesStr.toString(),
                        recursiveTimeMs, iterativeTimeMs, frameStewartTimeMs);
                System.out.println("Your score has been saved!");
            } else {
                System.out.println("\nGame ended. You did not complete the puzzle.");

                // Show correct solution
                System.out.println("\nCorrect solution:");
                for (int i = 0; i < Math.min(correctSolution.size(), 20); i++) {
                    System.out.println((i+1) + ". " + correctSolution.get(i));
                }

                if (correctSolution.size() > 20) {
                    System.out.println("... and " + (correctSolution.size() - 20) + " more moves");
                }

                // Build move sequence string
                StringBuilder movesStr = new StringBuilder();
                for (int i = 0; i < userMoves.size(); i++) {
                    movesStr.append(userMoves.get(i));
                    if (i < userMoves.size() - 1) {
                        movesStr.append(",");
                    }
                }

                // Save failed attempt to database
                saveResult(playerName, diskCount, pegCount, false, movesStr.toString(),
                        recursiveTimeMs, iterativeTimeMs, frameStewartTimeMs);
            }

        } catch (Exception e) {
            System.out.println("Error during interactive game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simulate a solution sequence on the towers
     */
    private static void simulateSolution(List<String> solution) {
        for (String move : solution) {
            char fromPeg = move.charAt(0);
            char toPeg = move.charAt(3);
            executeMove(fromPeg, toPeg);
        }
    }

    /**
     * Initialize the tower pegs for a new game
     */
    private static void initializeTowers(int diskCount, int pegCount) {
        pegs.clear();

        // Initialize all pegs as empty stacks
        for (char peg = 'A'; peg < 'A' + pegCount; peg++) {
            pegs.put(peg, new Stack<>());
        }

        // Place all disks on peg A
        Stack<Integer> pegA = pegs.get('A');
        for (int i = diskCount; i >= 1; i--) {
            pegA.push(i);
        }
    }

    /**
     * Execute a move between two pegs
     */
    private static void executeMove(char fromPeg, char toPeg) {
        if (pegs.get(fromPeg).isEmpty()) {
            throw new IllegalStateException("Cannot move from empty peg " + fromPeg);
        }

        int disk = pegs.get(fromPeg).pop();
        pegs.get(toPeg).push(disk);
    }

    /**
     * Check if a move from one peg to another is valid
     */
    private static boolean isValidMove(char fromPeg, char toPeg) {
        // Check if source peg has disks
        if (pegs.get(fromPeg).isEmpty()) {
            return false;
        }

        // Check if destination peg is empty or has larger disk on top
        if (pegs.get(toPeg).isEmpty()) {
            return true;
        } else {
            return pegs.get(fromPeg).peek() < pegs.get(toPeg).peek();
        }
    }

    /**
     * Check if the game is complete (all disks moved to the last peg)
     */
    private static boolean isGameComplete(int diskCount, int pegCount) {
        char lastPeg = (char)('A' + pegCount - 1);
        return pegs.get(lastPeg).size() == diskCount;
    }

    /**
     * Display the current state of all towers
     */
    private static void displayTowers(int pegCount) {
        System.out.println("\nCurrent tower state:");

        // Find the maximum height of any peg
        int maxHeight = 0;
        for (char peg = 'A'; peg < 'A' + pegCount; peg++) {
            maxHeight = Math.max(maxHeight, pegs.get(peg).size());
        }

        // Create 2D representation of pegs
        String[][] display = new String[maxHeight][pegCount];
        for (int i = 0; i < maxHeight; i++) {
            for (int j = 0; j < pegCount; j++) {
                display[i][j] = "      ";
            }
        }

        // Fill in the disks
        for (int pegIndex = 0; pegIndex < pegCount; pegIndex++) {
            char peg = (char)('A' + pegIndex);
            Stack<Integer> pegStack = pegs.get(peg);
            Integer[] disks = pegStack.toArray(new Integer[0]);

            for (int i = 0; i < disks.length; i++) {
                int disk = disks[disks.length - 1 - i];
                display[maxHeight - 1 - i][pegIndex] = String.format("[%2d]  ", disk);
            }
        }

        // Print the display
        for (int i = 0; i < maxHeight; i++) {
            for (int j = 0; j < pegCount; j++) {
                System.out.print(display[i][j]);
            }
            System.out.println();
        }

        // Print the peg labels
        for (int j = 0; j < pegCount; j++) {
            System.out.print(" Peg " + (char)('A' + j) + "  ");
        }
        System.out.println();
    }

    /**
     * Improved Recursive solution for Tower of Hanoi
     * Time complexity: O(2^n)
     * Space complexity: O(n) due to the recursion stack
     */
    public static List<String> solveRecursive(int n, char from, char aux, char to) {
        List<String> moves = new ArrayList<>();
        solveRecursiveHelper(n, from, aux, to, moves);
        return moves;
    }

    private static void solveRecursiveHelper(int n, char from, char aux, char to, List<String> moves) {
        if (n == 1) {
            // Base case: move one disk directly from source to destination
            moves.add(from + "->" + to);
            return;
        }

        // Step 1: Move n-1 disks from source to auxiliary peg
        solveRecursiveHelper(n - 1, from, to, aux, moves);

        // Step 2: Move the largest disk from source to destination
        moves.add(from + "->" + to);

        // Step 3: Move n-1 disks from auxiliary to destination peg
        solveRecursiveHelper(n - 1, aux, from, to, moves);
    }

    /**
     * Improved Iterative solution for Tower of Hanoi
     * This approach uses an explicit stack to simulate the recursion
     * Time complexity: O(2^n)
     * Space complexity: O(n)
     */
    public static List<String> solveIterative(int n, char from, char aux, char to) {
        List<String> moves = new ArrayList<>();
        Stack<HanoiState> stack = new Stack<>();

        // Push initial state
        stack.push(new HanoiState(n, from, aux, to, false));

        while (!stack.isEmpty()) {
            HanoiState current = stack.pop();

            if (current.n == 1) {
                // Base case: move disk directly
                moves.add(current.from + "->" + current.to);
            } else {
                if (current.processed) {
                    // This is the middle step (moving the largest disk)
                    moves.add(current.from + "->" + current.to);
                } else {
                    // Push the steps in reverse order (since stack is LIFO)
                    // Step 3: Move n-1 disks from aux to to using from
                    stack.push(new HanoiState(current.n - 1, current.aux, current.from, current.to, false));

                    // Step 2: Move the largest disk from from to to (mark as processed)
                    stack.push(new HanoiState(current.n, current.from, current.aux, current.to, true));

                    // Step 1: Move n-1 disks from from to aux using to
                    stack.push(new HanoiState(current.n - 1, current.from, current.to, current.aux, false));
                }
            }
        }

        return moves;
    }

    /**
     * Helper class for the iterative solution
     */
    private static class HanoiState {
        int n;
        char from, aux, to;
        boolean processed;

        HanoiState(int n, char from, char aux, char to, boolean processed) {
            this.n = n;
            this.from = from;
            this.aux = aux;
            this.to = to;
            this.processed = processed;
        }
    }

    /**
     * Frame-Stewart algorithm for the 4-peg Tower of Hanoi
     * This is a heuristic approach that is believed to be optimal
     * Time complexity: O(2^(n/2))
     */
    public static List<String> solveFrameStewart(int n, char source, char aux1, char aux2, char dest) {
        List<String> moves = new ArrayList<>();

        // Base case: for small numbers of disks, use the standard algorithm
        if (n == 0) {
            return moves;
        }
        if (n == 1) {
            moves.add(source + "->" + dest);
            return moves;
        }

        // Find optimal k (number of disks to move first)
        int k = findOptimalK(n);

        // Step 1: Move top k disks from source to aux1 using all four pegs
        moves.addAll(solveFrameStewart(k, source, aux2, dest, aux1));

        // Step 2: Move remaining n-k disks from source to dest using three pegs (standard algorithm)
        moves.addAll(solveRecursive(n - k, source, aux2, dest));

        // Step 3: Move k disks from aux1 to dest using all four pegs
        moves.addAll(solveFrameStewart(k, aux1, source, aux2, dest));

        return moves;
    }

    /**
     * Helper method to find optimal k for Frame-Stewart algorithm
     * The optimal k is floor(n/2) for most cases
     */
    private static int findOptimalK(int n) {
        return (int) Math.floor(n / 2.0);
    }

    /**
     * Compare performance of different algorithms
     */
    private static void compareAlgorithms() {
        System.out.println("\n⏱️ ALGORITHM COMPARISON ⏱️");
        System.out.println("==========================");

        System.out.print("Enter the number of disks (5-15): ");
        int diskCount = 0;
        try {
            diskCount = Integer.parseInt(scanner.nextLine());
            if (diskCount < 5 || diskCount > 15) {
                System.out.println("Number should be between 5 and 15. Using default of 8.");
                diskCount = 8;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default of 8 disks.");
            diskCount = 8;
        }

        System.out.println("\nComparing algorithms for " + diskCount + " disks...");

        // 3-peg solutions
        long recursiveStartTime = System.nanoTime();
        List<String> recursiveSolution = solveRecursive(diskCount, 'A', 'B', 'C');
        long recursiveEndTime = System.nanoTime();
        long recursiveTimeMs = (recursiveEndTime - recursiveStartTime) / 1_000_000;

        long iterativeStartTime = System.nanoTime();
        List<String> iterativeSolution = solveIterative(diskCount, 'A', 'B', 'C');
        long iterativeEndTime = System.nanoTime();
        long iterativeTimeMs = (iterativeEndTime - iterativeStartTime) / 1_000_000;

        // 4-peg solution (Frame-Stewart)
        long frameStewartStartTime = System.nanoTime();
        List<String> frameStewartSolution = solveFrameStewart(diskCount, 'A', 'B', 'C', 'D');
        long frameStewartEndTime = System.nanoTime();
        long frameStewartTimeMs = (frameStewartEndTime - frameStewartStartTime) / 1_000_000;

        // Display results
        System.out.println("\n📊 RESULTS:");
        System.out.println("3-Peg Recursive: " + recursiveTimeMs + " ms, " + recursiveSolution.size() + " moves");
        System.out.println("3-Peg Iterative: " + iterativeTimeMs + " ms, " + iterativeSolution.size() + " moves");
        System.out.println("4-Peg Frame-Stewart: " + frameStewartTimeMs + " ms, " + frameStewartSolution.size() + " moves");

        // Verify 3-peg solutions match
        if (!recursiveSolution.equals(iterativeSolution)) {
            System.out.println("\nWARNING: 3-peg solutions don't match!");
        } else {
            System.out.println("\n3-peg solutions match correctly ✓");
        }

        // Compare 3-peg and 4-peg solutions
        double improvementRatio = (double)recursiveSolution.size() / frameStewartSolution.size();
        System.out.printf("The 4-peg solution requires %.2f%% fewer moves than the 3-peg solution.%n",
                (1 - 1/improvementRatio) * 100);

        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    private static void viewHighScores() {
        System.out.println("\n🏆 HIGH SCORES 🏆");
        System.out.println("=================");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT player_name, disk_count, peg_count, timestamp FROM hanoi_results " +
                             "WHERE success = 1 ORDER BY disk_count DESC, peg_count DESC, timestamp ASC LIMIT 10")) {

            System.out.println("Rank | Player | Disks | Pegs | Date");
            System.out.println("----------------------------------");

            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("player_name");
                int disks = rs.getInt("disk_count");
                int pegs = rs.getInt("peg_count");
                String date = rs.getString("timestamp");

                System.out.printf("%-4d | %-6s | %-5d | %-4d | %s%n", rank++, name, disks, pegs, date);
            }

            if (rank == 1) {
                System.out.println("No high scores recorded yet!");
            }

        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    private static void showInstructions() {
        System.out.println("\n📖 TOWER OF HANOI INSTRUCTIONS 📖");
        System.out.println("=================================");
        System.out.println("Objective:");
        System.out.println("Move all disks from the starting peg to the destination peg");
        System.out.println("following these rules:");
        System.out.println("1. Only one disk can be moved at a time");
        System.out.println("2. A larger disk cannot be placed on top of a smaller disk");
        System.out.println("3. You can use the auxiliary pegs to help move the disks");

        System.out.println("\nGame Modes:");
        System.out.println("1. Classic Game (3 pegs) - The traditional Tower of Hanoi");
        System.out.println("2. Extended Game (4 pegs) - Uses Frame-Stewart algorithm");

        System.out.println("\nSolving Approaches:");
        System.out.println("1. Enter complete solution - Provide all moves at once");
        System.out.println("2. Make moves one by one - Interactive gameplay");

        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS hanoi_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_name TEXT NOT NULL," +
                    "disk_count INTEGER NOT NULL," +
                    "peg_count INTEGER NOT NULL," +
                    "success INTEGER NOT NULL," +
                    "move_sequence TEXT," +
                    "recursive_time_ms INTEGER," +
                    "iterative_time_ms INTEGER," +
                    "frame_stewart_time_ms INTEGER," +
                    "timestamp TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(createTableSQL);

        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            throw e;
        }
    }

    private static void saveResult(String playerName, int diskCount, int pegCount,
                                   boolean success, String moveSequence,
                                   long recursiveTimeMs, long iterativeTimeMs,
                                   long frameStewartTimeMs) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO hanoi_results (player_name, disk_count, peg_count, " +
                             "success, move_sequence, recursive_time_ms, iterative_time_ms, " +
                             "frame_stewart_time_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, playerName);
            pstmt.setInt(2, diskCount);
            pstmt.setInt(3, pegCount);
            pstmt.setInt(4, success ? 1 : 0);
            pstmt.setString(5, moveSequence);
            pstmt.setLong(6, recursiveTimeMs);
            pstmt.setLong(7, iterativeTimeMs);
            pstmt.setLong(8, frameStewartTimeMs);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving result to database: " + e.getMessage());
        }
    }
}