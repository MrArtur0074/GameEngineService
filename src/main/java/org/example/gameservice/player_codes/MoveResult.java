package org.example.gameservice.player_codes;

public class MoveResult {
    private final String direction;
    private final Object store;

    public MoveResult(String direction, Object store) {
        this.direction = direction;
        this.store = store;
    }

    public String getDirection() {
        return direction;
    }

    public Object getStore() {
        return store;
    }
}