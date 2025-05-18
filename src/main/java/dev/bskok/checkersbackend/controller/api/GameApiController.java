package dev.bskok.checkersbackend.controller.api;

import dev.bskok.checkersbackend.dtos.*;
import dev.bskok.checkersbackend.model.GameEntity;
import dev.bskok.checkersbackend.model.GameStatsEntity;
import dev.bskok.checkersbackend.model.PlayerEntity;
import dev.bskok.checkersbackend.service.GameService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameApiController {

    private final GameService gameService;

    @PostMapping
    public ResponseEntity<GameCreationResponse> createGame(@RequestBody CreateGameRequest request) {
        try {
            GameDTO game = gameService.createGame(request);

            if (game.getGameStats() == null || game.getGameStats().size() < 2) {
                throw new IllegalStateException("Game stats not initialized");
            }

            GameStatsDTO stats1 = game.getGameStats().get(0);
            GameStatsDTO stats2 = game.getGameStats().get(1);

            if (stats1.getPlayer() == null || stats2.getPlayer() == null) {
                throw new IllegalStateException("Players not associated with game stats");
            }

            GameCreationResponse response = new GameCreationResponse();
            response.setGame(game);
            response.setPlayer1Id(stats1.getPlayer().getId());
            response.setPlayer2Id(stats2.getPlayer().getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Game creation failed", e);
            return ResponseEntity.internalServerError()
                    .body(new GameCreationResponse());
        }
    }

    @PostMapping("/{gameId}/result")
    public ResponseEntity<GameDTO> submitGameResult(
            @PathVariable Long gameId,
            @RequestBody GameResultRequest request) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDTO> getGame(@PathVariable Long gameId) {
        try {
            return ResponseEntity.ok(gameService.getGameById(gameId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GameDTO>> getGamesByPlayer(@PathVariable Long playerId) {
        try {
            return ResponseEntity.ok(gameService.getGamesByPlayer(playerId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GameDTO>> getRecentGames(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(gameService.getRecentGames(limit));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long gameId) {
        try {
            gameService.deleteGame(gameId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
