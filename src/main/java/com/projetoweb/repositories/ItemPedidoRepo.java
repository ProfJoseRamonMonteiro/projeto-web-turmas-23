package com.projetoweb.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projetoweb.models.ItemPedidoModel;
import com.projetoweb.models.PedidoModel;

public interface ItemPedidoRepo extends JpaRepository<ItemPedidoModel, Long> {

    List<ItemPedidoModel> findByPedido(PedidoModel pedido);

    @Query("SELECT i FROM ItemPedidoModel i WHERE i.produto.idProduto = :idProduto")
    List<ItemPedidoModel> findByProdutoId(@Param("idProduto") Long idProduto);

}
