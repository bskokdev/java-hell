package dev.bskok.checkersbackend.repository;

import dev.bskok.checkersbackend.model.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
    List<GameEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<GameEntity> findByWinnerName(String winnerName);
    List<GameEntity> findByWinnerColor(String winnerColor);
}
