package com.projetoweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.projetoweb.models.UsuarioModel;
import com.projetoweb.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/alterar-senha")
    public String formAlterarSenha(HttpSession session, Model model) {
        UsuarioModel u = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (u == null) return "redirect:/login";
        model.addAttribute("usuario", u);
        return "/Usuario/alterar-senha"; // criar template
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(
            @RequestParam String novaSenha,
            HttpSession session,
            Model model
    ) {
        UsuarioModel u = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (u == null) return "redirect:/login";

        usuarioService.alterarSenha(u.getIdUsuario(), novaSenha);

        // atualizar sessão para refletir senhaTemporaria=false
        UsuarioModel atualizado = usuarioService.findByNomeUsuario(u.getNomeUsuario()).orElse(u);
        session.setAttribute("usuarioLogado", atualizado);

        return "redirect:/";
    }

}
