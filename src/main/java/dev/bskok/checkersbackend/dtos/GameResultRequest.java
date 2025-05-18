package dev.bskok.checkersbackend.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameResultRequest {
    private Long gameId;
    private String winnerName;
    private String winnerColor;
    private int winnerScore;
    private int winnerPieces;
    private String loserName;
    private int loserScore;
    private int loserPieces;
}
