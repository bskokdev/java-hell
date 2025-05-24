package dev.bskok.checkersbackend.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class HomeWebController {

    @GetMapping
    public String home(Model model) {
        return "home";
    }
}
