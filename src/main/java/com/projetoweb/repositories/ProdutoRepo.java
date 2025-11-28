package com.projetoweb.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoweb.models.CategoriaModel;
import com.projetoweb.models.ProdutoModel;

public interface ProdutoRepo extends JpaRepository<ProdutoModel, Long> {

    boolean existsByNomeIgnoreCase(String nome);
    List<ProdutoModel> findByCategoriaIdCategoria(Long idCategoria);
    List<ProdutoModel> findByCategoria(CategoriaModel categoria);
    List<ProdutoModel> findByEmPromocaoTrue();
    List<ProdutoModel> findByDestaqueTrue();
    List<ProdutoModel> findByNomeContainingIgnoreCase(String termo);
    Page<ProdutoModel> findByNomeContainingIgnoreCase(String termo, Pageable pageable);

}
