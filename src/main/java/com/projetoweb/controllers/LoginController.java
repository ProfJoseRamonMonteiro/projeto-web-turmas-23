package com.projetoweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "/Login/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String senha,
            Model model,
            HttpSession session) {

        return usuarioService.autenticar(username, senha)
                .map(u -> {
                    session.setAttribute("usuarioLogado", u);

                    if (u.getNivelAcesso().equals(NivelAcessoEnum.ADMINISTRADOR)) {
                        if (u.getPrimeiroAcesso()) {
                            return "redirect:/usuario/alterar-senha";
                        }
                        return "redirect:/admin/dashboard";
                    }

                    if (u.getNivelAcesso().equals(NivelAcessoEnum.CLIENTE)) {
                        return "redirect:/";
                    }

                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("erro", "Credenciais inválidas!");
                    return "/Login/login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/cadastro")
    public String cadastroPage() {
        return "/Usuario/cadastro";
    }

    @PostMapping("/cadastro/salvar")
    public String cadastro(
            @RequestParam String username,
            @RequestParam String senha,
            @RequestParam String nome,
            Model model) {
        try {
            usuarioService.registrarCliente(username, senha, nome);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "/Usuario/cadastro";
        }
    }
}
