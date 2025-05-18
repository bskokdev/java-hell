package dev.bskok.checkersbackend.service;

import dev.bskok.checkersbackend.dtos.PlayerDTO;
import dev.bskok.checkersbackend.model.PlayerEntity;
import dev.bskok.checkersbackend.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerDTO createPlayer(PlayerDTO playerDTO) {
        PlayerEntity player = new PlayerEntity();
        player.setName(playerDTO.getName());
        player.setColor(playerDTO.getColor());
        player.setTop(playerDTO.isTop());

        PlayerEntity savedPlayer = playerRepository.save(player);
        return new PlayerDTO(savedPlayer);
    }

    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public PlayerDTO getPlayerById(Long id) {
        return playerRepository.findById(id)
                .map(PlayerDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + id));
    }

    public PlayerDTO updatePlayer(Long id, PlayerDTO playerDTO) {
        PlayerEntity player = playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + id));

        player.setName(playerDTO.getName());
        player.setColor(playerDTO.getColor());
        player.setTop(playerDTO.isTop());

        PlayerEntity updatedPlayer = playerRepository.save(player);
        return new PlayerDTO(updatedPlayer);
    }

    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new EntityNotFoundException("Player not found with id: " + id);
        }
        playerRepository.deleteById(id);
    }
}
