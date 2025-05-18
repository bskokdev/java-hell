package dev.bskok.checkersbackend.dtos;

import dev.bskok.checkersbackend.model.GameStatsEntity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameStatsDTO {
    private Long id;
    private int score;
    private int piecesRemaining;
    private PlayerDTO player;

    public GameStatsDTO(GameStatsEntity gameStats) {
        this.id = gameStats.getId();
        this.score = gameStats.getScore();
        this.piecesRemaining = gameStats.getPiecesRemaining();
        this.player = new PlayerDTO(gameStats.getPlayer());
    }
}
