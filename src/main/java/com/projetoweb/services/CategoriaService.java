package com.projetoweb.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projetoweb.models.CategoriaModel;
import com.projetoweb.repositories.CategoriaRepo;

import jakarta.transaction.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepo categoriaRepository;

    public CategoriaService(CategoriaRepo categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaModel> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<CategoriaModel> porId(Long id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public CategoriaModel criar(CategoriaModel categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaModel atualizar(Long id, CategoriaModel dados) {
        CategoriaModel c = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        c.setNome(dados.getNome());
        c.setDescricao(dados.getDescricao());
        return categoriaRepository.save(c);
    }

    @Transactional
    public void deletar(Long id) {
        categoriaRepository.deleteById(id);
    }

}
