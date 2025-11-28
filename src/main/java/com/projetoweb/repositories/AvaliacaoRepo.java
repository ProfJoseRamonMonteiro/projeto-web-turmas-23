package com.projetoweb.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoweb.models.AvaliacaoModel;

public interface AvaliacaoRepo extends JpaRepository<AvaliacaoModel, Long> {

    boolean existsByClienteIdUsuarioAndProdutoIdProduto(Long idCliente, Long idProduto);

    AvaliacaoModel findByClienteIdUsuarioAndProdutoIdProduto(Long idCliente, Long idProduto);

    List<AvaliacaoModel> findByProdutoIdProdutoOrderByDataAvaliacaoDesc(Long idProduto);

}
