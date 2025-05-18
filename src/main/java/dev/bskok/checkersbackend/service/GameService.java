package dev.bskok.checkersbackend.service;

import dev.bskok.checkersbackend.dtos.CreateGameRequest;
import dev.bskok.checkersbackend.dtos.GameDTO;
import dev.bskok.checkersbackend.dtos.GameStatsDTO;
import dev.bskok.checkersbackend.dtos.PlayerDTO;
import dev.bskok.checkersbackend.model.GameEntity;
import dev.bskok.checkersbackend.model.GameStatsEntity;
import dev.bskok.checkersbackend.model.PlayerEntity;
import dev.bskok.checkersbackend.repository.GameRepository;
import dev.bskok.checkersbackend.repository.GameStatsRepository;
import dev.bskok.checkersbackend.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameStatsRepository gameStatsRepository;


    @Transactional
    public GameDTO createGame(CreateGameRequest request) {
        // Validation
        if (request.getPlayer1Name() == null || request.getPlayer2Name() == null) {
            throw new IllegalArgumentException("Player names cannot be null");
        }

        // 1. Create or find players
        PlayerEntity player1 = playerRepository.findByName(request.getPlayer1Name())
                .orElseGet(() -> {
                    PlayerEntity p = new PlayerEntity();
                    p.setName(request.getPlayer1Name());
                    p.setColor(request.getPlayer1Color());
                    p.setTop(request.isPlayer1IsTop());
                    return playerRepository.save(p); // Changed to save()
                });

        PlayerEntity player2 = playerRepository.findByName(request.getPlayer2Name())
                .orElseGet(() -> {
                    PlayerEntity p = new PlayerEntity();
                    p.setName(request.getPlayer2Name());
                    p.setColor(request.getPlayer2Color());
                    p.setTop(request.isPlayer2IsTop());
                    return playerRepository.save(p); // Changed to save()
                });

        // 2. Create game
        GameEntity game = new GameEntity();
        game.setStartTime(LocalDateTime.now());

        // 3. Create stats
        GameStatsEntity stats1 = new GameStatsEntity();
        stats1.setPlayer(player1);
        stats1.setGame(game); // Bidirectional relationship
        stats1.setPiecesRemaining(12);
        stats1.setScore(0);

        GameStatsEntity stats2 = new GameStatsEntity();
        stats2.setPlayer(player2);
        stats2.setGame(game); // Bidirectional relationship
        stats2.setPiecesRemaining(12);
        stats2.setScore(0);

        // Add stats to game (using the mutable collection)
        game.getGameStats().add(stats1);
        game.getGameStats().add(stats2);

        // Single save operation
        game = gameRepository.saveAndFlush(game);

        return convertToDto(game);
    }


    private GameDTO convertToDto(GameEntity game) {
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setStartTime(game.getStartTime());
        dto.setEndTime(game.getEndTime());
        dto.setWinnerName(game.getWinnerName());
        dto.setWinnerColor(game.getWinnerColor());

        if (game.getGameStats() != null) {
            List<GameStatsDTO> statsDtos = game.getGameStats().stream()
                    .map(stat -> {
                        GameStatsDTO statDto = new GameStatsDTO();
                        statDto.setId(stat.getId());
                        statDto.setScore(stat.getScore());
                        statDto.setPiecesRemaining(stat.getPiecesRemaining());

                        if (stat.getPlayer() != null) {
                            PlayerDTO playerDto = new PlayerDTO();
                            playerDto.setId(stat.getPlayer().getId());
                            playerDto.setName(stat.getPlayer().getName());
                            playerDto.setColor(stat.getPlayer().getColor());
                            playerDto.setTop(stat.getPlayer().isTop());
                            statDto.setPlayer(playerDto);
                        }

                        return statDto;
                    })
                    .collect(Collectors.toList());
            dto.setGameStats(statsDtos);
        }

        return dto;
    }

    private GameStatsDTO convertStatsToDto(GameStatsEntity stats) {
        GameStatsDTO statsDto = new GameStatsDTO();
        statsDto.setId(stats.getId());
        statsDto.setScore(stats.getScore());
        statsDto.setPiecesRemaining(stats.getPiecesRemaining());

        PlayerDTO playerDto = convertPlayerToDto(stats.getPlayer());
        statsDto.setPlayer(playerDto);

        return statsDto;
    }

    private PlayerDTO convertPlayerToDto(PlayerEntity player) {
        PlayerDTO playerDto = new PlayerDTO();
        playerDto.setId(player.getId());
        playerDto.setName(player.getName());
        playerDto.setColor(player.getColor());
        playerDto.setTop(player.isTop());
        return playerDto;
    }

    private PlayerEntity createNewPlayer(String name, String color, boolean isTop) {
        PlayerEntity player = new PlayerEntity();
        player.setName(name);
        player.setColor(color);
        player.setTop(isTop);
        return playerRepository.save(player);
    }

    public GameStatsEntity createGameStats(PlayerEntity player, GameEntity game, int initialPieces) {
        GameStatsEntity stats = new GameStatsEntity();
        stats.setPlayer(player);
        stats.setGame(game);
        stats.setScore(0);
        stats.setPiecesRemaining(initialPieces);
        return gameStatsRepository.save(stats);
    }

    public GameDTO updateGameResult(Long gameId,
                                    String winnerName, String winnerColor,
                                    int winnerScore, int winnerPieces,
                                    String loserName,
                                    int loserScore, int loserPieces) {

        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));

        game.setEndTime(LocalDateTime.now());
        game.setWinnerName(winnerName);
        game.setWinnerColor(winnerColor);

        GameStatsEntity winnerStats = game.getGameStats().stream()
                .filter(gs -> gs.getPlayer().getName().equals(winnerName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Winner stats not found"));

        GameStatsEntity loserStats = game.getGameStats().stream()
                .filter(gs -> gs.getPlayer().getName().equals(loserName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Loser stats not found"));

        winnerStats.setScore(winnerScore);
        winnerStats.setPiecesRemaining(winnerPieces);
        loserStats.setScore(loserScore);
        loserStats.setPiecesRemaining(loserPieces);

        return convertToDto(gameRepository.save(game));
    }

    public List<GameDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(GameDTO::new)
                .collect(Collectors.toList());
    }

    public GameDTO getGameById(Long id) {
        return gameRepository.findById(id)
                .map(GameDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));
    }

    public List<GameDTO> getGamesByPlayer(Long playerId) {
        return gameStatsRepository.findByPlayerId(playerId).stream()
                .map(GameStatsEntity::getGame)
                .distinct()
                .map(GameDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new EntityNotFoundException("Game not found with id: " + id);
        }
        gameRepository.deleteById(id);
    }

    public List<GameDTO> getRecentGames(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "endTime"));
        return gameRepository.findAll(pageable).stream()
            .map(GameDTO::new)
            .collect(Collectors.toList());
    }
}
