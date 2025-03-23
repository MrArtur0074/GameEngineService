package org.example.gameservice.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gameservice.controller.GameResult;
import org.example.gameservice.controller.Player;
import org.example.gameservice.player_codes.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class GameEngine {
    private static final String MAP_FILE = "src/main/resources/game_map.json";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_MOVES = 100;
    private Player player1 = new Player();
    private Player player2 = new Player();

    public GameResult calculateGame() {
        try {
            // Загружаем данные игры
            Map<String, Object> gameData = objectMapper.readValue(new File(MAP_FILE), Map.class);
            int[][] map = objectMapper.convertValue(gameData.get("map"), int[][].class);
            int playerMoves = (int) gameData.get("player_moves");
            int amountFood = (int) gameData.get("amount_food");

            Random random = new Random();
            boolean player1Starts = random.nextBoolean();

            ArrayList<String> moves1 = new ArrayList<>();
            ArrayList<String> moves2 = new ArrayList<>();

            // Загружаем классы динамически
            URL classUrl = new File("target/classes/").toURI().toURL();
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});

            // Получаем классы Player1 и Player2
            Class<?> store1Class = Class.forName("org.example.gameservice.player_codes.Store1", true, classLoader);
            Class<?> store2Class = Class.forName("org.example.gameservice.player_codes.Store2", true, classLoader);
            Class<?> player1Class = Class.forName("org.example.gameservice.player_codes.Player1", true, classLoader);
            Class<?> player2Class = Class.forName("org.example.gameservice.player_codes.Player2", true, classLoader);


            Object store1 = store1Class.getDeclaredConstructor().newInstance();
            Object store2 = store2Class.getDeclaredConstructor().newInstance();

            // Получаем методы movePlayer
            Method movePlayerMethod1 = player1Class.getMethod("movePlayer", int[][].class, store1Class);
            Method movePlayerMethod2 = player2Class.getMethod("movePlayer", int[][].class, store2Class);

            while (playerMoves < MAX_MOVES) {
                try {
                    if (player1Starts) {
                        try {
                            // Создаем экземпляр Player1
                            Object player1Instance = player1Class.getDeclaredConstructor().newInstance();
                            // Вызов метода movePlayer для Player1
                            MoveResult result1 = (MoveResult) movePlayerMethod1.invoke(player1Instance, map, store1);
                            System.out.println(result1);
                            updateMap(map, result1.getDirection(), 2, this.player1);
                            store1 = result1.getStore();
                            moves1.add(result1.getDirection());
                        } catch (Exception e) {
                            moves1.add("null");
                            System.err.println("Ошибка у Player1: " + e.getMessage());
                        }
                    } else {
                        try {
                            // Создаем экземпляр Player2
                            Object player2Instance = player2Class.getDeclaredConstructor().newInstance();
                            // Вызов метода movePlayer для Player2
                            MoveResult result2 = (MoveResult) movePlayerMethod2.invoke(player2Instance, map, store2);
                            System.out.println(result2);
                            updateMap(map, result2.getDirection(), 3, this.player2);
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

    private static void updateMap(int[][] map, String direction, int playerSymbol, Player player) {
        int playerX = -1, playerY = -1;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == playerSymbol) {
                    playerX = i;
                    playerY = j;
                    break;
                }
            }
        }

        if (playerX == -1 || playerY == -1) return;

        int newX = playerX, newY = playerY;

        switch (direction) {
            case "left": newY--; break;
            case "right": newY++; break;
            case "top": newX--; break;
            case "bottom": newX++; break;
        }

        if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length && map[newX][newY] != 0) {
            if (map[newX][newY] == 3) {
                player.setScore(player.getScore() + 10);
            }
            map[playerX][playerY] = 1;
            map[newX][newY] = playerSymbol;
        }
    }
}