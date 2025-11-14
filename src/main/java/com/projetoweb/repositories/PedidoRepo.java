package com.projetoweb.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.projetoweb.models.PedidoModel;

public interface PedidoRepo extends JpaRepository<PedidoModel, Long> {

    @Query(value = "SELECT MONTH(p.data_criacao) AS mes, SUM(p.total) AS total "
            + "FROM Pedidos p "
            + "GROUP BY MONTH(p.data_criacao) "
            + "ORDER BY mes;", nativeQuery = true)
    List<Object[]> findVendasPorAnoEMes();

}
