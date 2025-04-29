
package org.example.algoplay.models;

import java.time.LocalDateTime;

    public class TicTacToeRound {
        private int roundId;
        private int gameId;
        private int userId;
        private String result; // "win", "loss", "draw"
        private LocalDateTime createdAt;
        private String difficulty;

        public TicTacToeRound() {
            this.createdAt = LocalDateTime.now();
        }

        public TicTacToeRound(int gameId, int userId, String result, String difficulty) {
            this.gameId = gameId;
            this.userId = userId;
            this.result = result;
            this.difficulty = difficulty;
            this.createdAt = LocalDateTime.now();
        }

        // Getters and setters
        public int getRoundId() {
            return roundId;
        }

        public void setRoundId(int roundId) {
            this.roundId = roundId;
        }

        public int getGameId() {
            return gameId;
        }

        public void setGameId(int gameId) {
            this.gameId = gameId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }
    }

