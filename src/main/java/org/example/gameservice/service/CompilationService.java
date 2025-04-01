package org.example.gameservice.service;

import org.example.gameservice.dto.GameResult;
import org.example.gameservice.dto.PlayerCodeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.example.gameservice.game.GameEngine;

@Service
public class CompilationService {
    private static final String CODE_DIRECTORY = "src/main/java/org/example/gameservice/player_codes/";
    private static final String CLASS_OUTPUT_DIR = "target/classes/";
    private static final String CLASS_OUTPUT_DIR_2 = "target/classes/org/example/gameservice/player_codes/";

    public GameResult compileAndRunBattle(PlayerCodeRequest request) {
        try {
            String file1 = saveCodeToFile(fixBraces(request.getPlayer1()), "Player1");
            String file2 = saveCodeToFile(fixBraces(request.getStore1()), "Store1");
            String file3 = saveCodeToFile(fixBraces(request.getPlayer2()), "Player2");
            String file4 = saveCodeToFile(fixBraces(request.getStore2()), "Store2");

            int[][] gameMap = request.getMap();

            if (gameMap == null || gameMap.length == 0 || gameMap[0].length == 0) {
                throw new IllegalArgumentException("Карта не может быть пустой");
            }

            boolean success2 = compileJavaFile(file2);
            boolean success4 = compileJavaFile(file4);
            boolean success1 = false;
            boolean success3 = false;

            if (success2 && success4) {
                success1 = compileJavaFile(file1);
                success3 = compileJavaFile(file3);
            }

            boolean player1Compiled = success1 && success2;
            boolean player2Compiled = success3 && success4;

            if (player1Compiled && player2Compiled) {
                GameEngine gameEngine = new GameEngine();
                GameResult result = gameEngine.calculateGame(gameMap);
                return result;
            } else if (player1Compiled) {
                return new GameResult(
                        "Player1",
                        1, // Победа игрока 1
                        0, // Поражение игрока 2
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "false",
                        "Ошибка компиляции 2-го игрока"
                );
            } else if (player2Compiled) {
                return new GameResult(
                        "Player2",
                        0, // Поражение игрока 1
                        1, // Победа игрока 2
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "true",
                        "Ошибка компиляции 1-го игрока"
                );
            } else {
                return new GameResult(
                        "Draw",
                        0,
                        0,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "N/A",
                        "Ошибка компиляции 2-х игроков]"
                );
            }
        } catch (IOException e) {
            ResponseEntity.badRequest().body(new GameResult(
                    "draw",
                    0,
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "N/A",
                    "Ошибка при обработке кода: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return new GameResult(
                    "draw",
                    0,
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "N/A",
                    "Ошибка валидации карты: " + e.getMessage()
            );
        } finally {
            resetFiles(new String[]{"Player1.java", "Store1.java", "Player2.java", "Store2.java"}, CODE_DIRECTORY);
            resetFiles(new String[]{"Player1.class", "Store1.class", "Player2.class", "Store2.class"}, CLASS_OUTPUT_DIR_2);
        }

        return null;
    }

    private String saveCodeToFile(String code, String className) throws IOException {
        Files.createDirectories(Paths.get(CODE_DIRECTORY));
        String fileName = CODE_DIRECTORY + className + ".java";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(code + "\n");
        }
        return fileName;
    }

    private void resetFiles(String[] fileNames, String directory) {
        for (String fileName : fileNames) {
            File file = new File(directory + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private boolean compileJavaFile(String filePath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, "-d", CLASS_OUTPUT_DIR, filePath) == 0;
    }

    private String fixBraces(String code) {
        if (code == null) {
            return "";
        }
        long openBraces = code.chars().filter(ch -> ch == '{').count();
        long closeBraces = code.chars().filter(ch -> ch == '}').count();
        if (openBraces > closeBraces) {
            code += "\n}";
        }
        return code;
    }
}

