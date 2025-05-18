package dev.bskok.checkersbackend.repository;

import dev.bskok.checkersbackend.model.GameStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameStatsRepository extends JpaRepository<GameStatsEntity, Long> {
    List<GameStatsEntity> findByPlayerId(Long playerId);

    @Query("SELECT gs FROM GameStatsEntity gs WHERE gs.game.id = :gameId AND gs.player.id = :playerId")
    Optional<GameStatsEntity> findByGameIdAndPlayerId(@Param("gameId") Long gameId,
                                                    @Param("playerId") Long playerId);
    @Query("SELECT gs FROM GameStatsEntity gs WHERE gs.game.id = :gameId")
    List<GameStatsEntity> findAllByGameId(@Param("gameId") Long gameId);
}
