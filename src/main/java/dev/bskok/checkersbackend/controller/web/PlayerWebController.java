package dev.bskok.checkersbackend.controller.web;

import dev.bskok.checkersbackend.dtos.GameDTO;
import dev.bskok.checkersbackend.dtos.PlayerDTO;
import dev.bskok.checkersbackend.service.GameService;
import dev.bskok.checkersbackend.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/new")
    public String showCreatePlayerForm(Model model) {
        model.addAttribute("player", new PlayerDTO());
        return "create-player";
    }

    @PostMapping
    public String createPlayer(@ModelAttribute PlayerDTO playerDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            playerService.createPlayer(playerDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Player created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating player: " + e.getMessage());
        }
        return "redirect:/web/players";
    }

    @PostMapping("/{id}/delete")
    public String deletePlayer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            playerService.deletePlayer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Player deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting player: " + e.getMessage());
        }
        return "redirect:/web/players";
    }

    @GetMapping("/{id}/edit")
    public String showEditPlayerForm(@PathVariable Long id, Model model) {
        PlayerDTO player = playerService.getPlayerById(id);
        model.addAttribute("player", player);
        return "edit-player";
    }

    @PutMapping("/{id}")
    public String updatePlayer(@PathVariable Long id,
                             @ModelAttribute PlayerDTO playerDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            playerService.updatePlayer(id, playerDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Player updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating player: " + e.getMessage());
        }
        return "redirect:/web/players";
    }
}
