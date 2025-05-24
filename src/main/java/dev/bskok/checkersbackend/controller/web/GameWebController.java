package dev.bskok.checkersbackend.controller.web;

import dev.bskok.checkersbackend.dtos.GameDTO;
import dev.bskok.checkersbackend.service.GameService;
import dev.bskok.checkersbackend.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/web/games")
@RequiredArgsConstructor
public class GameWebController {
    private final GameService gameService;
    private final PlayerService playerService;

    @GetMapping
    public String showAllGames(Model model) {
        model.addAttribute("games", gameService.getAllGames());
        return "games";
    }

    @GetMapping("/{id}")
    public String showGameDetails(@PathVariable Long id, Model model) {
        model.addAttribute("game", gameService.getGameById(id));
        return "game-details";
    }

    @GetMapping("/new")
    public String showCreateGameForm(Model model) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setStartTime(LocalDateTime.now());
        model.addAttribute("game", gameDTO);
        model.addAttribute("players", playerService.getAllPlayers());
        return "create-game";
    }

    @PostMapping
    public String createGame(@ModelAttribute GameDTO gameDTO, RedirectAttributes redirectAttributes) {
        try {
            gameService.createGame(gameDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Game created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating game: " + e.getMessage());
        }
        return "redirect:/web/games";
    }

    @PostMapping("/{id}/delete")
    public String deleteGame(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            gameService.deleteGame(id);
            redirectAttributes.addFlashAttribute("successMessage", "Game deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting game: " + e.getMessage());
        }
        return "redirect:/web/games";
    }

    @GetMapping("/{id}/edit")
    public String showEditGameForm(@PathVariable Long id, Model model) {
        GameDTO game = gameService.getGameById(id);
        model.addAttribute("game", game);
        model.addAttribute("players", playerService.getAllPlayers());
        return "edit-game";
    }

    @PutMapping("/{id}")
    public String updateGame(@PathVariable Long id,
                           @ModelAttribute GameDTO gameDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            gameService.updateGame(id, gameDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Game updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating game: " + e.getMessage());
        }
        return "redirect:/web/games";
    }
}
