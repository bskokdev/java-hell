package dev.bskok.checkersbackend.controller.api;

import dev.bskok.checkersbackend.dtos.GameDTO;
import dev.bskok.checkersbackend.dtos.GameResultRequest;
import dev.bskok.checkersbackend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-results")
public class GameResultApiController {
    private final GameService gameService;

    @Autowired
    public GameResultApiController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDTO> submitGameResult(@RequestBody GameResultRequest request) {
        GameDTO game = gameService.updateGameResult(
                request.getGameId(),
                request.getWinnerName(),
                request.getWinnerColor(),
                request.getWinnerScore(),
                request.getWinnerPieces(),
                request.getLoserName(),
                request.getLoserScore(),
                request.getLoserPieces()
        );
        return ResponseEntity.ok(game);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GameDTO>> getPlayerGameHistory(@PathVariable Long playerId) {
        return ResponseEntity.ok(gameService.getGamesByPlayer(playerId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GameDTO>> getRecentGames(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(gameService.getRecentGames(limit));
    }
}
