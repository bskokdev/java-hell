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
        if (request.getPlayer1Name() == null || request.getPlayer2Name() == null) {
            throw new IllegalArgumentException("Player names cannot be null");
        }

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

        GameEntity game = new GameEntity();
        game.setStartTime(LocalDateTime.now());

        GameStatsEntity stats1 = new GameStatsEntity();
        stats1.setPlayer(player1);
        stats1.setGame(game);
        stats1.setPiecesRemaining(12);
        stats1.setScore(0);

        GameStatsEntity stats2 = new GameStatsEntity();
        stats2.setPlayer(player2);
        stats2.setGame(game);
        stats2.setPiecesRemaining(12);
        stats2.setScore(0);

        game.getGameStats().add(stats1);
        game.getGameStats().add(stats2);

        game = gameRepository.saveAndFlush(game);
        return convertToDto(game);
    }

    @Transactional
    public GameDTO createGame(GameDTO gameDTO) {
        if (gameDTO.getGameStats() == null || gameDTO.getGameStats().size() != 2) {
            throw new IllegalArgumentException("Game must have exactly 2 players");
        }

        GameEntity game = new GameEntity();
        game.setStartTime(gameDTO.getStartTime() != null ?
                gameDTO.getStartTime() : LocalDateTime.now());
        game.setEndTime(gameDTO.getEndTime());
        game.setWinnerName(gameDTO.getWinnerName());
        game.setWinnerColor(gameDTO.getWinnerColor());

        for (GameStatsDTO statsDTO : gameDTO.getGameStats()) {
            if (statsDTO.getPlayer() == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }

            if (statsDTO.getPlayer().getColor() == null) {
                statsDTO.getPlayer().setColor("#000000"); // default
            }

            PlayerEntity player = playerRepository.findById(statsDTO.getPlayer().getId())
                    .orElseGet(() -> {
                        PlayerEntity newPlayer = new PlayerEntity();
                        newPlayer.setName(statsDTO.getPlayer().getName());
                        newPlayer.setColor(statsDTO.getPlayer().getColor());
                        newPlayer.setTop(statsDTO.getPlayer().isTop());
                        return playerRepository.save(newPlayer);
                    });

            GameStatsEntity gameStats = new GameStatsEntity();
            gameStats.setPlayer(player);
            gameStats.setGame(game);
            gameStats.setScore(statsDTO.getScore());
            gameStats.setPiecesRemaining(statsDTO.getPiecesRemaining());

            game.getGameStats().add(gameStats);
        }

        return new GameDTO(gameRepository.save(game));
    }

    public GameDTO updateGame(Long id, GameDTO gameDTO) {
        GameEntity existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));

        existingGame.setStartTime(gameDTO.getStartTime());
        existingGame.setEndTime(gameDTO.getEndTime());
        existingGame.setWinnerName(gameDTO.getWinnerName());
        existingGame.setWinnerColor(gameDTO.getWinnerColor());

        if (gameDTO.getGameStats() != null && gameDTO.getGameStats().size() == 2) {
            // clear existing stats to avoid duplicates (they will be recreated)
            existingGame.getGameStats().clear();
            gameStatsRepository.deleteById(existingGame.getId());

            for (GameStatsDTO statsDTO : gameDTO.getGameStats()) {
                if (statsDTO.getPlayer() == null) {
                    throw new IllegalArgumentException("Player cannot be null");
                }

                PlayerEntity player = playerRepository.findById(statsDTO.getPlayer().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Player not found with id: " + statsDTO.getPlayer().getId()));

                GameStatsEntity gameStats = new GameStatsEntity();
                gameStats.setPlayer(player);
                gameStats.setGame(existingGame);
                gameStats.setScore(statsDTO.getScore());
                gameStats.setPiecesRemaining(statsDTO.getPiecesRemaining());

                existingGame.getGameStats().add(gameStats);
            }
        } else {
            throw new IllegalArgumentException("Game must have exactly 2 players");
        }

        GameEntity updatedGame = gameRepository.save(existingGame);
        return convertToDto(updatedGame);
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
