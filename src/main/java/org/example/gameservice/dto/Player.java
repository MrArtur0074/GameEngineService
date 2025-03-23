package org.example.gameservice.dto;

public class Player {
    private int score = 0;
    private int moves = 0;
    private int number;
    private int playerSymbol;

    public Player(int number, int playerSymbol) {
        this.number = number;
        this.playerSymbol = playerSymbol;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getNumber() {
        return number;
    }

    public int getPlayerSymbol() {
        return playerSymbol;
    }
}
