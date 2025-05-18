package dev.bskok.checkersbackend.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateGameRequest {
    private String player1Name;
    private String player1Color;
    private boolean player1IsTop;
    private String player2Name;
    private String player2Color;
    private boolean player2IsTop;
}
