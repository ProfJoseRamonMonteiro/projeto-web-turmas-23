package com.projetoweb.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projetoweb.dto.VendaMesDTO;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.services.CategoriaService;
import com.projetoweb.services.PedidoService;
import com.projetoweb.services.ProdutoService;
import com.projetoweb.services.VendaService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final VendaService vendaService;

    public AdminController(PedidoService pedidoService, ProdutoService produtoService, CategoriaService categoriaService, VendaService vendaService) {
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.vendaService = vendaService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<VendaMesDTO> vendasPorMes = pedidoService.vendasPorMes();
        model.addAttribute("vendas", vendasPorMes);
        List<ProdutoModel> produtos = produtoService.listarTodos();
        model.addAttribute("produtos", produtos);
        return "/Admin/dashboard";
    }

    @GetMapping("/api/vendas-mensais")
    @ResponseBody
    public Map<String, Double> obterVendasMensais() {
        return vendaService.obterTotaisMensais();
    }

    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "/Admin/produtos";
    }

    @GetMapping("/produtos/novo")
    public String novoProduto(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("produto", new ProdutoModel());
        return "/Admin/produto-novo";
    }

    @PostMapping("/produtos/novo")
    public String salvarProduto(ProdutoModel produto) {
        produtoService.criar(produto);
        return "redirect:/admin/produtos";
    }

    @GetMapping("/produtos/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model) {
        ProdutoModel produto = produtoService.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/produto-editar";
    }

    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable Long id, ProdutoModel produto) {
        produtoService.atualizar(id, produto);
        return "redirect:/admin/produtos";
    }

    @PostMapping("/produtos/remover/{id}")
    public String deletarProduto(@PathVariable Long id) {
        produtoService.deletar(id);
        return "redirect:/admin/produtos";
    }

}
