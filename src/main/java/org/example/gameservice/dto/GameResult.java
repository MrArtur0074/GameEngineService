package org.example.gameservice.dto;

import java.util.List;

public class GameResult {
    private final String winner;
    private final int scorePlayer1;
    private final int scorePlayer2;
    private final List<String> movesPlayer1;
    private final List<String> movesPlayer2;
    private final String firstPlayer;
    private final String message;

    public GameResult(String winner, int scorePlayer1, int scorePlayer2,
                      List<String> movesPlayer1, List<String> movesPlayer2, String firstPlayer, String message) {
        this.winner = winner;
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
        this.movesPlayer1 = movesPlayer1;
        this.movesPlayer2 = movesPlayer2;
        this.firstPlayer = firstPlayer;
        this.message = message;
    }

    public String getWinner() {
        return winner;
    }

    public int getScorePlayer1() {
        return scorePlayer1;
    }

    public int getScorePlayer2() {
        return scorePlayer2;
    }

    public List<String> getMovesPlayer1() {
        return movesPlayer1;
    }

    public List<String> getMovesPlayer2() {
        return movesPlayer2;
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }
}
