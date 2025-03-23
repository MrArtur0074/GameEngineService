package org.example.gameservice.controller;

import org.example.gameservice.dto.GameResult;
import org.example.gameservice.dto.PlayerCodeRequest;
import org.example.gameservice.service.CompilationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/game")
public class GameController {
    private final CompilationService compilationService;
    public GameController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public ResponseEntity<GameResult> compileCode(@RequestBody PlayerCodeRequest request) {
        if (request.getPlayer1() == null || request.getStore1() == null || request.getPlayer2() == null || request.getStore2() == null) {
            return ResponseEntity.badRequest().body(new GameResult(
                    "draw",
                    0,
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "N/A",
                    "Ошибка: код одного или нескольких игроков отсутствует"
            ));
        }

        GameResult result = compilationService.compileAndRunBattle(request);

        return ResponseEntity.ok(result);
    }
}

