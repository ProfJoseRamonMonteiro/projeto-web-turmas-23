package com.projetoweb.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.projetoweb.models.CategoriaModel;
import com.projetoweb.models.ItemPedidoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.repositories.CategoriaRepo;
import com.projetoweb.repositories.ItemPedidoRepo;
import com.projetoweb.repositories.ProdutoRepo;

import jakarta.transaction.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepo produtoRepository;
    private final CategoriaRepo categoriaRepository;
    private final ItemPedidoRepo itemPedidoRepo;

    public ProdutoService(ProdutoRepo produtoRepository, CategoriaRepo categoriaRepository,
            ItemPedidoRepo itemPedidoRepo) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.itemPedidoRepo = itemPedidoRepo;
    }

    /*
     * ===========================
     * CONSULTAS
     * ===========================
     */
    public List<ProdutoModel> listarTodos() {
        return produtoRepository.findAll();
    }

    public ProdutoModel porId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    public List<ProdutoModel> buscarPorCategoria(CategoriaModel categoria) {
        return produtoRepository.findByCategoria(categoria);
    }

    public List<ProdutoModel> listarPromocoes() {
        return produtoRepository.findByEmPromocaoTrue();
    }

    public List<ProdutoModel> listarDestaques() {
        return produtoRepository.findByDestaqueTrue();
    }

    public Page<ProdutoModel> listarPaginado(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        return produtoRepository.findAll(pageable);
    }

    /*
     * ===========================
     * CRIAÇÃO
     * ===========================
     */
    @Transactional
    public ProdutoModel criar(ProdutoModel p) {

        if (produtoRepository.existsByNomeIgnoreCase(p.getNome())) {
            throw new IllegalArgumentException("Produto com este nome já existe!");
        }

        // Categoria válida
        if (p.getCategoria() != null && p.getCategoria().getIdCategoria() != null) {
            CategoriaModel cat = categoriaRepository.findById(p.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
            p.setCategoria(cat);
        } else {
            p.setCategoria(null);
        }

        // Se está em promoção mas não enviou preço promocional → erro
        if (p.isEmPromocao() && p.getPrecoPromocional() == null) {
            throw new IllegalArgumentException("Preço promocional deve ser informado quando em promoção.");
        }

        return produtoRepository.save(p);
    }

    /*
     * ===========================
     * ATUALIZAÇÃO
     * ===========================
     */
    @Transactional
    public ProdutoModel atualizar(Long id, ProdutoModel dados, Long idCategoria) {

        ProdutoModel p = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        p.setNome(dados.getNome());
        p.setDescricao(dados.getDescricao());
        p.setPreco(dados.getPreco());
        p.setEstoque(dados.getEstoque());
        p.setEmPromocao(dados.isEmPromocao());
        p.setPrecoPromocional(dados.getPrecoPromocional());
        p.setDestaque(dados.isDestaque());

        // Atualiza imagem somente se enviada
        if (dados.getImagem() != null && dados.getImagem().length > 0) {
            p.setImagem(dados.getImagem());
        }

        // Atualiza categoria
        if (idCategoria != null) {
            CategoriaModel cat = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria inválida"));
            p.setCategoria(cat);
        } else {
            // Categoria pode ser removida
            p.setCategoria(null);
        }

        // Validação de promoção
        if (p.isEmPromocao() && p.getPrecoPromocional() == null) {
            throw new IllegalArgumentException("Preço promocional deve ser informado quando em promoção.");
        }

        return produtoRepository.save(p);
    }

    /*
     * ===========================
     * EXCLUSÃO
     * ===========================
     */
    public void excluirProduto(Long idProduto) {

        // 1. Remover referência do Produto em todos os ItensPedido
        List<ItemPedidoModel> itens = itemPedidoRepo.findByProdutoId(idProduto);
        for (ItemPedidoModel item : itens) {
            item.setProduto(null); // deixa o campo null
        }
        itemPedidoRepo.saveAll(itens);

        // 2. Agora pode excluir o produto
        produtoRepository.deleteById(idProduto);
    }

}
