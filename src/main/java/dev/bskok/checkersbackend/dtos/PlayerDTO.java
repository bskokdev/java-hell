package dev.bskok.checkersbackend.dtos;

import dev.bskok.checkersbackend.model.PlayerEntity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    private Long id;
    private String name;
    private String color;
    private boolean isTop;

    public PlayerDTO(PlayerEntity player) {
        this.id = player.getId();
        this.name = player.getName();
        this.color = player.getColor();
        this.isTop = player.isTop();
    }
}
