package com.projetoweb.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projetoweb.models.CategoriaModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.repositories.CategoriaRepo;
import com.projetoweb.repositories.ProdutoRepo;

import jakarta.transaction.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepo produtoRepository;
    private final CategoriaRepo categoriaRepository;
    private final CategoriaService categoriaService;

    public ProdutoService(ProdutoRepo produtoRepository, CategoriaRepo categoriaRepository, CategoriaService categoriaService) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
    }

    public List<ProdutoModel> listarTodos() {
        return produtoRepository.findAll();
    }

    public List<ProdutoModel> buscarPorCategoria(Long categoriaId) {
        CategoriaModel cat = categoriaService.porId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        return produtoRepository.findByCategoria(cat);
    }

    public List<ProdutoModel> listarPromocoes() {
        return produtoRepository.findByEmPromocaoTrue();
    }

    public List<ProdutoModel> listarDestaques() {
        return produtoRepository.findByDestaqueTrue();
    }

    public Optional<ProdutoModel> porId(Long id) {
        return produtoRepository.findById(id);
    }

    @Transactional
    public ProdutoModel criar(ProdutoModel p) {
        if (produtoRepository.existsByNome(p.getNome())) {
            throw new IllegalArgumentException("Produto já existe!");
        }
        if (!categoriaRepository.existsByNome(p.getCategoria().getNome())) {
            throw new IllegalArgumentException("Categoria não existe!");
        }
        return produtoRepository.save(p);
    }

    @Transactional
    public ProdutoModel atualizar(Long id, ProdutoModel dados) {
        ProdutoModel p = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        p.setNome(dados.getNome());
        p.setDescricao(dados.getDescricao());
        p.setPreco(dados.getPreco());
        p.setEstoque(dados.getEstoque());
        p.setEmPromocao(dados.isEmPromocao());
        p.setDestaque(dados.isDestaque());
        p.setImagem(dados.getImagem());
        p.setCategoria(dados.getCategoria());
        return produtoRepository.save(p);
    }

    @Transactional
    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }

}
