package dev.bskok.checkersbackend.controller.web;

import dev.bskok.checkersbackend.dtos.GameDTO;
import dev.bskok.checkersbackend.dtos.PlayerDTO;
import dev.bskok.checkersbackend.service.GameService;
import dev.bskok.checkersbackend.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/web/players")
public class PlayerWebController {
    private final PlayerService playerService;
    private final GameService gameService;

    @Autowired
    public PlayerWebController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @GetMapping
    public String showAllPlayers(Model model) {
        model.addAttribute("players", playerService.getAllPlayers());
        return "players";
    }

    @GetMapping("/{id}")
    public String showPlayerDetails(@PathVariable Long id, Model model) {
        PlayerDTO player = playerService.getPlayerById(id);
        List<GameDTO> games = gameService.getGamesByPlayer(id);

        long winCount = games.stream()
                .filter(game -> game != null && game.getWinnerName() != null)
                .filter(game -> game.getWinnerName().equals(player.getName()))
                .count();

        model.addAttribute("player", player);
        model.addAttribute("games", games);
        model.addAttribute("winCount", winCount);
        return "player-details";
    }
}
