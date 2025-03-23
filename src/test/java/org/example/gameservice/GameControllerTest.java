package org.example.gameservice;

import org.junit.jupiter.api.Test; // Импорт для JUnit 5
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers; // Импорт для проверки результата

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPlayGame() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/game")
                        .param("player1", "package org.example.gameservice.player_codes; import java.util.Random; public class Player1 { private static final Random random = new Random(); public MoveResult movePlayer(int[][] map, Store1 store) { return new MoveResult(generateDirection(), store); } private String generateDirection() { return new String[]{\\\"right\\\", \\\"left\\\", \\\"top\\\", \\\"bottom\\\"}[random.nextInt(4)]; } }")
                        .param("store1", "package org.example.gameservice.player_codes; public class Store1 {}")
                        .param("player2", "package org.example.gameservice.player_codes; import java.util.Random; public class Player2 { private static final Random random = new Random(); public MoveResult movePlayer(int[][] map, Store2 store) { String[] directions = {\\\"right\\\", \\\"left\\\", \\\"top\\\", \\\"bottom\\\"}; String chosenDirection = directions[random.nextInt(directions.length)]; return new MoveResult(chosenDirection, store); } }")
                        .param("store2", "package org.example.gameservice.player_codes; public class Store2 {}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
