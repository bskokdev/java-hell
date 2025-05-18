package dev.bskok.checkersbackend.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameCreationResponse {
    private GameDTO game;
    private Long player1Id;
    private Long player2Id;
}
