package dev.bskok.checkersbackend.controller.web;

import dev.bskok.checkersbackend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/games")
public class GameWebController {
    private final GameService gameService;

    @Autowired
    public GameWebController(GameService gameService) {
        this.gameService = gameService;
    }

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
}
