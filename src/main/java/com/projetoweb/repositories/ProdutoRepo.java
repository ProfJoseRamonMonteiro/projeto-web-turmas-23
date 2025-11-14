package com.projetoweb.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoweb.models.CategoriaModel;
import com.projetoweb.models.ProdutoModel;

public interface ProdutoRepo extends JpaRepository<ProdutoModel, Long> {

    boolean existsByNome(String nome);
    List<ProdutoModel> findByIdProduto(Long idProduto);
    List<ProdutoModel> findByCategoria(CategoriaModel categoria);
    List<ProdutoModel> findByEmPromocaoTrue();
    List<ProdutoModel> findByDestaqueTrue();
    List<ProdutoModel> findByNomeContainingIgnoreCase(String termo);

}
