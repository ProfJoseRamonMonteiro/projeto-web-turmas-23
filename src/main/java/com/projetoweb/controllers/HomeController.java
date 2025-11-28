package com.projetoweb.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projetoweb.dao.CarrinhoItem;
import com.projetoweb.dto.AvaliacaoDTO;
import com.projetoweb.models.ItemPedidoModel;
import com.projetoweb.models.PedidoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.repositories.ProdutoRepo;
import com.projetoweb.repositories.UsuarioRepo;
import com.projetoweb.services.AvaliacaoService;
import com.projetoweb.services.CategoriaService;
import com.projetoweb.services.PedidoService;
import com.projetoweb.services.ProdutoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final AvaliacaoService avaliacaoService;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final PedidoService pedidoService;

    private final UsuarioRepo usuarioRepo;
    private final ProdutoRepo produtoRepo;

    @SuppressWarnings("unused")
    private UsuarioModel usuarioModel;

    private int i;

    public HomeController(AvaliacaoService avaliacaoService,
            ProdutoService produtoService,
            CategoriaService categoriaService,
            ProdutoRepo produtoRepo,
            PedidoService pedidoService,
            UsuarioRepo usuarioRepo) {
        this.avaliacaoService = avaliacaoService;
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.pedidoService = pedidoService;
        this.usuarioRepo = usuarioRepo;
        this.produtoRepo = produtoRepo;
    }

    private List<String> gerarEstrelasMedia(double media) {
        List<String> estrelas = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            if (i <= Math.floor(media)) {
                estrelas.add("fa-star");
            } else if (i == Math.ceil(media) && (media % 1) >= 0.5) {
                estrelas.add("fa-star-half-o");
            } else {
                estrelas.add("fa-star-o");
            }
        }
        return estrelas;
    }

    @GetMapping(value = { "/", "/home" })
    public String index(HttpSession session, Model model) {
        usuarioModel = (UsuarioModel) session.getAttribute("usuarioLogado");
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("promocoes", produtoService.listarPromocoes());
        model.addAttribute("destaques", produtoService.listarDestaques());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "/Home/home";
    }

    @PostMapping("/produto/detalhado")
    public String produtoDetalhado(@RequestParam("idProduto") Long idProduto, Model model) {

        ProdutoModel produto = produtoService.porId(idProduto);
        List<AvaliacaoDTO> avaliacoes = avaliacaoService.listarPorProduto(idProduto);
        double media = avaliacaoService.mediaAvaliacoes(idProduto);

        Map<Integer, Long> qtdPorEstrela = new HashMap<>();
        for (i = 1; i <= 5; i++) {
            long qtd = avaliacoes.stream().filter(a -> a.getEstrelas() == i).count();
            qtdPorEstrela.put(i, qtd);
        }

        List<String> estrelasMedia = gerarEstrelasMedia(media);
        long totalAvaliacoes = avaliacoes.size();

        Map<Integer, Double> percentualPorEstrela = new HashMap<>();
        for (int j = 1; j <= 5; j++) {
            long qtd = qtdPorEstrela.get(j);
            double percentual = totalAvaliacoes == 0 ? 0 : (qtd * 100.0 / totalAvaliacoes);
            percentualPorEstrela.put(j, percentual);
        }

        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("produtos", produtoService.buscarPorCategoria(produto.getCategoria()));

        model.addAttribute("avaliacoes", avaliacoes);
        model.addAttribute("media", media);
        model.addAttribute("qtdPorEstrela", qtdPorEstrela);
        model.addAttribute("percentualPorEstrela", percentualPorEstrela);
        model.addAttribute("estrelasMedia", estrelasMedia);
        model.addAttribute("qtdAvaliacoes", totalAvaliacoes);

        return "/Produto/produto-detalhado";
    }

    @GetMapping("/imagem/{idProduto}")
    @ResponseBody
    public byte[] exibirImagem(@PathVariable("idProduto") Long idProduto) {
        ProdutoModel produto = produtoService.porId(idProduto);
        return produto.getImagem();
    }

    @PostMapping("/pedido/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarPedido(
            @RequestBody List<CarrinhoItem> itens,
            HttpSession session) {

        if (itens == null || itens.isEmpty()) {
            return ResponseEntity.badRequest().body("Carrinho vazio");
        }

        // Recupera o usuário logado da sessão
        UsuarioModel usuarioLogado = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).body("Usuário não está autenticado!");
        }

        // Busca o cliente no banco
        UsuarioModel cliente = usuarioRepo.findById(usuarioLogado.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado no banco"));

        // Converte CarrinhoItem → ItemPedidoModel
        List<ItemPedidoModel> itensPedido = itens.stream().map(ci -> {
            // Busca o produto real no banco
            ProdutoModel produto = produtoRepo.findById(ci.getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + ci.getIdProduto()));

            ItemPedidoModel item = new ItemPedidoModel();
            item.setProduto(produto);
            item.setQuantidade(ci.getQuantidade());

            // Preço sempre definido pelo produto real
            item.setPrecoUnitario(produto.getPreco());

            return item;
        }).toList();

        try {
            // Cria o pedido
            PedidoModel pedidoSalvo = pedidoService.criarPedido(cliente, itensPedido);

            // Limpa o carrinho da sessão
            session.removeAttribute("carrinho");

            return ResponseEntity.ok(pedidoSalvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno ao salvar pedido: " + e.getMessage());
        }
    }

}
