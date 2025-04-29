package org.example.algoplay.games.ttt;

import org.example.algoplay.models.TicTacToeBoard;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TicTacToeBoardTest {
    private TicTacToeBoard board;

    @BeforeEach
    void setUp() {
        board = new TicTacToeBoard(5);
    }

    @Test
    void testPlaceMoveAndIsCellEmpty() {
        assertTrue(board.isCellEmpty(0, 0));
        board.placeMove(0, 0, 'X');
        assertFalse(board.isCellEmpty(0, 0));
    }

    @Test
    void testRemoveMove() {
        board.placeMove(1, 1, 'X');
        assertFalse(board.isCellEmpty(1, 1));

        board.removeMove(1, 1);
        assertTrue(board.isCellEmpty(1, 1));
    }

    @Test
    void testWinInRow() {
        for (int i = 0; i < 5; i++) {
            board.placeMove(2, i, 'X');
        }
        assertTrue(board.checkWin('X'));
    }

    @Test
    void testWinInColumn() {
        for (int i = 0; i < 5; i++) {
            board.placeMove(i, 3, 'O');
        }
        assertTrue(board.checkWin('O'));
    }

    @Test
    void testWinInDiagonalTopLeftToBottomRight() {
        for (int i = 0; i < 5; i++) {
            board.placeMove(i, i, 'X');
        }
        assertTrue(board.checkWin('X'));
    }

    @Test
    void testWinInDiagonalTopRightToBottomLeft() {
        for (int i = 0; i < 5; i++) {
            board.placeMove(i, 4 - i, 'O');
        }
        assertTrue(board.checkWin('O'));
    }

    @Test
    void testBoardIsFull() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                board.placeMove(i, j, 'X');
            }
        }
        assertTrue(board.isFull());
    }

    @Test
    void testBoardIsNotFull() {
        board.placeMove(0, 0, 'X');
        assertFalse(board.isFull());
    }
}
