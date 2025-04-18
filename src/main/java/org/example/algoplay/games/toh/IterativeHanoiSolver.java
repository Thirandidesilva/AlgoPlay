package org.example.algoplay.games.toh;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IterativeHanoiSolver {

    public List<String> solve(int n) {
        List<String> moves = new ArrayList<>();

        // For iterative solution, we use stacks to keep track of function calls
        Stack<IterativeFrame> stack = new Stack<>();
        stack.push(new IterativeFrame(n, 0, 2, 1, FrameState.START));

        while (!stack.isEmpty()) {
            IterativeFrame frame = stack.pop();

            switch (frame.state) {
                case START:
                    if (frame.n == 1) {
                        moves.add("Move disk 1 from peg " + (char)('A' + frame.source) +
                                " to peg " + (char)('A' + frame.destination));
                    } else {
                        // Push frames in reverse order of execution
                        stack.push(new IterativeFrame(frame.n-1, frame.auxiliary, frame.destination,
                                frame.source, FrameState.START));
                        stack.push(new IterativeFrame(frame.n, frame.source, frame.destination,
                                frame.auxiliary, FrameState.MOVE_LARGEST));
                        stack.push(new IterativeFrame(frame.n-1, frame.source, frame.auxiliary,
                                frame.destination, FrameState.START));
                    }
                    break;

                case MOVE_LARGEST:
                    moves.add("Move disk " + frame.n + " from peg " + (char)('A' + frame.source) +
                            " to peg " + (char)('A' + frame.destination));
                    break;
            }
        }

        return moves;
    }

    private enum FrameState {
        START, MOVE_LARGEST
    }

    private static class IterativeFrame {
        int n;
        int source;
        int destination;
        int auxiliary;
        FrameState state;

        public IterativeFrame(int n, int source, int destination, int auxiliary, FrameState state) {
            this.n = n;
            this.source = source;
            this.destination = destination;
            this.auxiliary = auxiliary;
            this.state = state;
        }
    }
}