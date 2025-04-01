package org.example.gameservice.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gameservice.dto.GameResult;
import org.example.gameservice.dto.Player;
import org.example.gameservice.player_codes.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class GameEngine {
    public static final int EMPTY_FIELD = 0;
    public static final int WALL = 1;
    public static final int FOOD = 3;
    public static final int PLAYER_1 = 2;
    public static final int PLAYER_2 = 4;
    public static final int outOfBoundsValue = 9;
    private static final String MAP_FILE = "src/main/resources/game_map.json";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_MOVES = 100;
    private Player player1 = new Player(1, PLAYER_1);
    private Player player2 = new Player(2, PLAYER_2);

    public GameResult calculateGame(int[][] gameMap) {
        try {
            int[][] map = gameMap;
            int playerMoves = 0;

            Random random = new Random();
            boolean player1Starts = random.nextBoolean();

            ArrayList<String> moves1 = new ArrayList<>();
            ArrayList<String> moves2 = new ArrayList<>();

            URL classUrl = new File("target/classes/").toURI().toURL();
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});

            Class<?> store1Class = Class.forName("org.example.gameservice.player_codes.Store1", true, classLoader);
            Class<?> store2Class = Class.forName("org.example.gameservice.player_codes.Store2", true, classLoader);
            Class<?> player1Class = Class.forName("org.example.gameservice.player_codes.Player1", true, classLoader);
            Class<?> player2Class = Class.forName("org.example.gameservice.player_codes.Player2", true, classLoader);


            Object store1 = store1Class.getDeclaredConstructor().newInstance();
            Object store2 = store2Class.getDeclaredConstructor().newInstance();

            Method movePlayerMethod1 = player1Class.getMethod("movePlayer", int[][].class, store1Class);
            Method movePlayerMethod2 = player2Class.getMethod("movePlayer", int[][].class, store2Class);

            while (playerMoves < MAX_MOVES) {
                try {
                    if (player1Starts) {
                        try {
                            Object player1Instance = player1Class.getDeclaredConstructor().newInstance();
                            int[][] newMap = calculateMapArea(map, this.player1);
                            MoveResult result1 = (MoveResult) movePlayerMethod1.invoke(player1Instance, newMap, store1);
                            updateMap(map, result1.getDirection(), this.player1);
                            result1.setDirection(checkMove(result1.getDirection()));
                            store1 = result1.getStore();
                            moves1.add(result1.getDirection());
                        } catch (Exception e) {
                            moves1.add("null");
                            System.err.println("Ошибка у Player1: " + e.getMessage());
                        }
                    } else {
                        try {
                            Object player2Instance = player2Class.getDeclaredConstructor().newInstance();
                            int[][] newMap = calculateMapArea(map, this.player2);
                            MoveResult result2 = (MoveResult) movePlayerMethod2.invoke(player2Instance, newMap, store2);
                            updateMap(map, result2.getDirection(), this.player2);
                            result2.setDirection(checkMove(result2.getDirection()));
                            store2 = result2.getStore();
                            moves2.add(result2.getDirection());
                        } catch (Exception e) {
                            moves2.add("null");
                            System.err.println("Ошибка у Player2: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка во время игры: " + e.getMessage());
                }

                player1Starts = !player1Starts;
                playerMoves++;
            }

            String winner = (player1.getScore() > player2.getScore()) ? "Player1" : (player2.getScore() > player1.getScore())? "Player2" : "draw";
            return new GameResult(winner, player1.getScore(), player2.getScore(), moves1, moves2, "" + player1Starts, "");
        } catch (Exception e) {
            e.printStackTrace();
            GameResult errorResult = new GameResult(
                    "Draw",
                    0,
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "N/A",
                    "Произошла ошибка во время выполнения"
            );

            return errorResult;
        }
    }

    private static void updateMap(int[][] map, String direction, Player player) {
        int playerX = -1, playerY = -1;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == player.getPlayerSymbol()) {
                    playerX = i;
                    playerY = j;
                    break;
                }
            }
        }

        if (playerX == -1 || playerY == -1) return;

        int newX = playerX, newY = playerY;

        direction = direction.toLowerCase();

        switch (direction) {
            case "left": newY--; break;
            case "right": newY++; break;
            case "top": newX--; break;
            case "bottom": newX++; break;
        }

        if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length && map[newX][newY] != 0 && (map[newX][newY] == 0 || map[newX][newY] == 1 || map[newX][newY] == 3 )) {
            if (map[newX][newY] == 3) {
                player.setScore(player.getScore() + 10);
            }
            map[playerX][playerY] = 0;
            map[newX][newY] = player.getPlayerSymbol();
        }
    }

    private static int[][] calculateMapArea(int[][] map, Player player) {
        int rows = map.length;
        int cols = map[0].length;
        int playerX = -1, playerY = -1;

        outerLoop:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == player.getPlayerSymbol()) {
                    playerX = i;
                    playerY = j;
                    break outerLoop;
                }
            }
        }

        if (playerX == -1) return null;

        int[][] result = new int[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int mapX = playerX + i - 2;
                int mapY = playerY + j - 2;

                if (mapX < 0 || mapX >= rows || mapY < 0 || mapY >= cols) {
                    result[i][j] = outOfBoundsValue;
                } else {
                    result[i][j] = map[mapX][mapY];
                }
            }
        }

        return result;
    }

    private static String checkMove(String move) {
        if (move == null) {
            return null;
        }
        move = move.toLowerCase();

        return switch (move) {
            case "left", "right", "top", "bottom" -> move;
            default -> null;
        };
    }
}