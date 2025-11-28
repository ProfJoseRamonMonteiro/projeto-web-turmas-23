package com.projetoweb.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoweb.dto.CategoriaEdicaoDTO;
import com.projetoweb.dto.ItemPedidoDTO;
import com.projetoweb.dto.ProdutoEdicaoDTO;
import com.projetoweb.dto.VendaMesDTO;
import com.projetoweb.models.CategoriaModel;
import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.PedidoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.repositories.CategoriaRepo;
import com.projetoweb.repositories.PedidoRepo;
import com.projetoweb.services.CategoriaService;
import com.projetoweb.services.ItemPedidoService;
import com.projetoweb.services.PedidoService;
import com.projetoweb.services.ProdutoService;
import com.projetoweb.services.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final ItemPedidoService itemPedidoService;

    private final CategoriaRepo categoriaRepo; // usado para listagens simples
    private final PedidoRepo pedidoRepo; // usado apenas para paginação/listagens

    @Autowired
    private ObjectMapper mapper;

    public AdminController(PedidoService pedidoService,
            ProdutoService produtoService,
            CategoriaService categoriaService,
            CategoriaRepo categoriaRepo,
            UsuarioService usuarioService,
            PedidoRepo pedidoRepo,
            ItemPedidoService itemPedidoService) {
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.itemPedidoService = itemPedidoService;
        this.categoriaRepo = categoriaRepo;
        this.pedidoRepo = pedidoRepo;
    }

    @RequestMapping(value = "/dashboard", method = { RequestMethod.GET, RequestMethod.POST })
    public String dashboard(
            @RequestParam(defaultValue = "1") int pageProdutos,
            @RequestParam(defaultValue = "5") int sizeProdutos,
            @RequestParam(defaultValue = "1") int pageCategorias,
            @RequestParam(defaultValue = "5") int sizeCategorias,
            @RequestParam(defaultValue = "1") int pageClientes,
            @RequestParam(defaultValue = "5") int sizeClientes,
            @RequestParam(defaultValue = "1") int pagePedidos,
            @RequestParam(defaultValue = "5") int sizePedidos,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            Model model) throws JsonProcessingException {

        // Resumo
        model.addAttribute("totalAno", pedidoService.faturamentoAnual());
        model.addAttribute("totalPedidos", pedidoService.totalPedidos());
        model.addAttribute("totalClientes", usuarioService.totalClientes());
        model.addAttribute("faturamentoMes", pedidoService.faturamentoMesAtual());

        // Filtros
        model.addAttribute("anosDisponiveis", pedidoService.getAnosDisponiveis());
        model.addAttribute("mesesDisponiveis", pedidoService.getMesesMap());
        model.addAttribute("mesSelecionado", mes);
        model.addAttribute("anoSelecionado", ano);

        // Dados do gráfico
        LocalDate hoje = LocalDate.now();
        int anoAtual = (ano != null) ? ano : hoje.getYear();
        int mesAtual = (mes != null) ? mes : hoje.getMonthValue();

        List<VendaMesDTO> vendas = pedidoService.vendasPorMes(mesAtual, anoAtual);

        model.addAttribute("meses", mapper.writeValueAsString(vendas.stream().map(VendaMesDTO::getMesNome).toList()));
        model.addAttribute("totais", mapper.writeValueAsString(vendas.stream().map(VendaMesDTO::getTotal).toList()));

        // Paginação - Clientes
        Pageable pageableClientes = PageRequest.of(Math.max(0, pageClientes - 1), sizeClientes);
        Page<UsuarioModel> paginaClientes = usuarioService.listarPorNivel(NivelAcessoEnum.CLIENTE, pageableClientes);
        model.addAttribute("clientes", paginaClientes.getContent());
        model.addAttribute("paginaAtualClientes", pageClientes);
        model.addAttribute("totalPaginasClientes", paginaClientes.getTotalPages());
        model.addAttribute("totalItensClientes", paginaClientes.getTotalElements());
        model.addAttribute("itensPaginaClientes", paginaClientes.getNumberOfElements());
        model.addAttribute("sizeClientes", sizeClientes);

        // Paginação - Pedidos (repo, pois não há método no service)
        Pageable pageablePedidos = PageRequest.of(Math.max(0, pagePedidos - 1), sizePedidos);
        Page<PedidoModel> paginaPedidos = pedidoRepo.findAll(pageablePedidos);
        model.addAttribute("pedidos", paginaPedidos.getContent());
        model.addAttribute("paginaAtualPedidos", pagePedidos);
        model.addAttribute("totalPaginasPedidos", paginaPedidos.getTotalPages());
        model.addAttribute("totalItensPedidos", paginaPedidos.getTotalElements());
        model.addAttribute("itensPaginaPedidos", paginaPedidos.getNumberOfElements());
        model.addAttribute("sizePedidos", sizePedidos);

        // Paginação - Produtos (uso service que já fornece paginação)
        Page<ProdutoModel> paginaProdutos = produtoService.listarPaginado(Math.max(0, pageProdutos - 1), sizeProdutos);
        model.addAttribute("produtos", paginaProdutos.getContent());
        model.addAttribute("paginaAtualProdutos", pageProdutos);
        model.addAttribute("totalPaginasProdutos", paginaProdutos.getTotalPages());
        model.addAttribute("totalItensProdutos", paginaProdutos.getTotalElements());
        model.addAttribute("itensPaginaProdutos", paginaProdutos.getNumberOfElements());
        model.addAttribute("sizeProdutos", sizeProdutos);

        // Paginação - Categorias (repo)
        Pageable pageableCategorias = PageRequest.of(Math.max(0, pageCategorias - 1), sizeCategorias);
        Page<CategoriaModel> paginaCategorias = categoriaRepo.findAll(pageableCategorias);
        model.addAttribute("categorias", paginaCategorias.getContent());
        model.addAttribute("paginaAtualCategorias", pageCategorias);
        model.addAttribute("totalPaginasCategorias", paginaCategorias.getTotalPages());
        model.addAttribute("totalItensCategorias", paginaCategorias.getTotalElements());
        model.addAttribute("itensPaginaCategorias", paginaCategorias.getNumberOfElements());
        model.addAttribute("sizeCategorias", sizeCategorias);

        model.addAttribute("sizesClientes", List.of(5, 10, 25, 50, 100));
        model.addAttribute("sizesPedidos", List.of(5, 10, 25, 50, 100));
        model.addAttribute("sizesProd", List.of(5, 10, 25, 50, 100));
        model.addAttribute("sizesCat", List.of(5, 10, 25, 50, 100));

        model.addAttribute("abaAtiva", "clientes");

        return "/Admin/dashboard";
    }

    @GetMapping("/produtos/novo")
    public String novoProduto(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("produto", new ProdutoModel());
        return "/Produto/novo-produto";
    }

    @PostMapping("/produtos/novo/salvar")
    public String salvarProduto(
            @Valid @ModelAttribute("produto") ProdutoModel produtoModel,
            BindingResult result,
            @RequestParam("imagemFile") MultipartFile imagemFile,
            @RequestParam(value = "idCategoria", required = false) Long idCategoria,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("produto", produtoModel);
            return "/Produto/novo-produto";
        }

        if (idCategoria == null || idCategoria <= 0) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("produto", produtoModel);
            model.addAttribute("erroCategoria", "Selecione uma categoria válida.");
            return "/Produto/novo-produto";
        }

        CategoriaModel categoria = categoriaService.porId(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        produtoModel.setCategoria(categoria);

        if (!produtoModel.isEmPromocao()) {
            produtoModel.setPrecoPromocional(null);
        }

        try {
            if (imagemFile != null && !imagemFile.isEmpty()) {
                produtoModel.setImagem(imagemFile.getBytes());
            }
        } catch (IOException e) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("produto", produtoModel);
            model.addAttribute("erro", "Erro ao salvar imagem.");
            return "/Produto/novo-produto";
        }

        produtoService.criar(produtoModel);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/produtos/editar")
    public String carregarEdicao(@RequestParam Long idProduto, Model model) {

        ProdutoModel produto = produtoService.porId(idProduto);

        ProdutoEdicaoDTO dto = new ProdutoEdicaoDTO();
        dto.setIdProduto(produto.getIdProduto());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setEstoque(produto.getEstoque());
        dto.setEmPromocao(produto.isEmPromocao());
        dto.setPrecoPromocional(produto.getPrecoPromocional());
        dto.setDestaque(produto.isDestaque());
        if (produto.getCategoria() != null)
            dto.setIdCategoria(produto.getCategoria().getIdCategoria());

        model.addAttribute("produtoDTO", dto);
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "/Produto/editar-produto";
    }

    @PostMapping("/produtos/editar/salvar")
    public String salvarEdicao(
            @ModelAttribute ProdutoEdicaoDTO dto,
            @RequestParam("imagem") MultipartFile arquivoImagem) throws IOException {

        ProdutoModel produto = produtoService.porId(dto.getIdProduto());

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        produto.setEmPromocao(dto.isEmPromocao());
        produto.setPrecoPromocional(dto.getPrecoPromocional());
        produto.setDestaque(dto.isDestaque());

        if (dto.getIdCategoria() != null) {
            produto.setCategoria(categoriaService.porId(dto.getIdCategoria()).orElse(null));
        } else {
            produto.setCategoria(null);
        }

        if (!arquivoImagem.isEmpty()) {
            produto.setImagem(arquivoImagem.getBytes());
        }

        produtoService.atualizar(dto.getIdProduto(), produto, dto.getIdCategoria());
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/produtos/remover")
    public String remover(@RequestParam Long idProduto) {
        try {
            produtoService.excluirProduto(idProduto);
        } catch (DataIntegrityViolationException ex) {
            // Se houver FK, você pode redirecionar com mensagem de erro (implementação
            // simples)
            return "redirect:/admin/dashboard?erroRemover=true";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/categorias/nova")
    public String novaCategoria(Model model) {
        model.addAttribute("categoria", new CategoriaModel());
        return "/Categoria/nova-categoria";
    }

    @PostMapping("/categorias/nova/salvar")
    public String salvarCategoria(
            @Valid @ModelAttribute("categoria") CategoriaModel categoriaModel,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categoria", categoriaModel);
            return "/Categoria/nova-categoria";
        }

        categoriaService.criar(categoriaModel);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/categorias/editar")
    public String editarCategoria(@RequestParam Long idCategoria, Model model) {

        CategoriaModel categoria = categoriaService.porId(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        CategoriaEdicaoDTO dto = new CategoriaEdicaoDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNome(categoria.getNome());
        dto.setDescricao(categoria.getDescricao());

        model.addAttribute("categoriaDTO", dto);
        model.addAttribute("categoria", categoria);

        return "/Categoria/editar-categoria";
    }

    @PostMapping("/categorias/editar/salvar")
    public String salvarAlteracaoCategoria(@ModelAttribute CategoriaEdicaoDTO dto) {

        CategoriaModel categoria = categoriaService.porId(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());

        categoriaService.atualizar(dto.getIdCategoria(), categoria);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/categorias/remover")
    public String removerCategoria(@RequestParam Long idCategoria) {
        categoriaService.deletar(idCategoria);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/pedidos/{idPedido}/itens")
    @ResponseBody
    public List<ItemPedidoDTO> listarItensPedido(@PathVariable Long idPedido) {
        return itemPedidoService.buscarItensPorPedido(idPedido);
    }

    @PostMapping("/pedidos/{id}/enviar")
    @ResponseBody
    public ResponseEntity<String> enviarPedido(@PathVariable Long id) {
        try {
            pedidoService.enviarPedido(id);
            return ResponseEntity.ok("Pedido enviado.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao enviar pedido: " + e.getMessage());
        }
    }

    @PostMapping("/cliente/toggle/{idUsuario}")
    @ResponseBody
    public ResponseEntity<?> toggleCliente(@PathVariable Long idUsuario) {
        try {
            UsuarioModel u = usuarioService.toggleHabilitado(idUsuario);
            return ResponseEntity.ok(Map.of("success", true, "habilitado", u.isHabilitado()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
