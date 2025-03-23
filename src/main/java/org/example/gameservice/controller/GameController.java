package org.example.gameservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.gameservice.game.GameEngine;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@RestController
@RequestMapping("/game")
public class GameController {
    private static final String CODE_DIRECTORY = "src/main/java/org/example/gameservice/player_codes/";
    private static final String CLASS_OUTPUT_DIR = "target/classes/";
    private static final String CLASS_OUTPUT_DIR_2 = "target/classes/org/example/gameservice/player_codes/";

    @PostMapping
    public ResponseEntity<GameResult> compileCode(@RequestBody PlayerCodeRequest request) {
        try {
            if (request.getPlayer1() == null || request.getStore1() == null || request.getPlayer2() == null || request.getStore2() == null) {
                ResponseEntity.badRequest().body(new GameResult(
                        "draw",
                        0,
                        0,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "N/A",
                        "Ошибка: код одного или нескольких игроков отсутствует"
                ));
            }

            // Сохраняем код в файлы
            String file1 = saveCodeToFile(fixBraces(request.getPlayer1()), "Player1");
            String file2 = saveCodeToFile(fixBraces(request.getStore1()), "Store1");
            String file3 = saveCodeToFile(fixBraces(request.getPlayer2()), "Player2");
            String file4 = saveCodeToFile(fixBraces(request.getStore2()), "Store2");

            // Компилируем все файлы
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
                // Оба игрока скомпилировались успешно - запускаем игру
                GameEngine gameEngine = new GameEngine();
                GameResult result = gameEngine.calculateGame();
                return ResponseEntity.ok(result);
            } else if (player1Compiled) {
                // Игрок 1 скомпилировался, игрок 2 проигрывает
                return ResponseEntity.ok(new GameResult(
                        "Player1",
                        1, // Победа игрока 1
                        0, // Поражение игрока 2
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "false",
                        "Ошибка компиляции 2-го игрока"
                ));
            } else if (player2Compiled) {
                // Игрок 2 скомпилировался, игрок 1 проигрывает
                return ResponseEntity.ok(new GameResult(
                        "Player2",
                        0, // Поражение игрока 1
                        1, // Победа игрока 2
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "true",
                        "Ошибка компиляции 1-го игрока"
                ));
            } else {
                // Оба игрока не скомпилировались - игра невозможна
                return ResponseEntity.badRequest().body(new GameResult(
                        "Draw",
                        0,
                        0,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "N/A",
                        "Ошибка компиляции 2-х игроков]"
                ));
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
        } finally {
            // Удаляем файлы и создаем новые с шаблонным кодом
            resetPlayerCodes();
            deleteCompiledClasses();
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

    private void resetPlayerCodes() {
        String[] fileNames = {"Player1.java", "Store1.java", "Player2.java", "Store2.java"};

        for (String fileName : fileNames) {
            File file = new File(CODE_DIRECTORY + fileName);
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

    private void deleteCompiledClasses() {
        String[] fileNames = {"Player1.class", "Store1.class", "Player2.class", "Store2.class"};

        for (String fileName : fileNames) {
            File file = new File(CLASS_OUTPUT_DIR_2 + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}

