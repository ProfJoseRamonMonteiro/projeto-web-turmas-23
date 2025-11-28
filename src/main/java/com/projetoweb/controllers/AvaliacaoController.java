package com.projetoweb.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projetoweb.models.AvaliacaoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.services.AvaliacaoService;
import com.projetoweb.services.ProdutoService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final ProdutoService produtoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService, ProdutoService produtoService) {
        this.avaliacaoService = avaliacaoService;
        this.produtoService = produtoService;
    }

    @PostMapping("/salvar")
    public ResponseEntity<String> salvarAvaliacao(
            @RequestParam Long idProduto,
            @RequestParam Integer estrelas,
            @RequestParam(required = false) String comentario,
            HttpSession session) {

        UsuarioModel usuario = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuário não logado");
        }

        ProdutoModel produto;
        try {
            produto = produtoService.porId(idProduto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Produto não encontrado");
        }

        AvaliacaoModel av = new AvaliacaoModel();
        av.setCliente(usuario);
        av.setProduto(produto);
        av.setComentario(comentario);
        av.setEstrelas(estrelas);

        try {
            avaliacaoService.salvar(av);
            return ResponseEntity.ok("Avaliação salva com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao salvar avaliação");
        }
    }
}
