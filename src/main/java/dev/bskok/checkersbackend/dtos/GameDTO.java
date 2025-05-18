package dev.bskok.checkersbackend.dtos;

import dev.bskok.checkersbackend.model.GameEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String winnerName;
    private String winnerColor;
    private List<GameStatsDTO> gameStats;

    public GameDTO(GameEntity game) {
        this.id = game.getId();
        this.startTime = game.getStartTime();
        this.endTime = game.getEndTime();
        this.winnerName = game.getWinnerName();
        this.winnerColor = game.getWinnerColor();
        this.gameStats = game.getGameStats().stream()
                .map(GameStatsDTO::new)
                .collect(Collectors.toList());
    }
}
