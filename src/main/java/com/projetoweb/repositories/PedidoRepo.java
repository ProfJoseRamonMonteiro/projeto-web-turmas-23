package com.projetoweb.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projetoweb.dto.VendaMesProjecaoDTO;
import com.projetoweb.models.PedidoModel;

public interface PedidoRepo extends JpaRepository<PedidoModel, Long> {

        // 🔹 Faturamento anual (com COALESCE para evitar retorno null)
        @Query("""
                        SELECT COALESCE(SUM(p.total), 0)
                        FROM PedidoModel p
                        WHERE YEAR(p.dataCriacao) = YEAR(CURRENT_DATE)
                        """)
        BigDecimal faturamentoAnual();

        // 🔹 Faturamento mês atual
        @Query("""
                        SELECT COALESCE(SUM(p.total), 0)
                        FROM PedidoModel p
                        WHERE MONTH(p.dataCriacao) = MONTH(CURRENT_DATE)
                        AND YEAR(p.dataCriacao) = YEAR(CURRENT_DATE)
                        """)
        BigDecimal faturamentoMesAtual();

        // 🔹 Projeção: Total vendido por mês do ano atual (Ordenado)
        @Query("""
                        SELECT new com.projetoweb.dto.VendaMesProjecaoDTO(
                        MONTH(p.dataCriacao),
                        SUM(p.total)
                        )
                        FROM PedidoModel p
                        WHERE YEAR(p.dataCriacao) = YEAR(CURRENT_DATE)
                        GROUP BY MONTH(p.dataCriacao)
                        ORDER BY MONTH(p.dataCriacao)
                        """)
        List<VendaMesProjecaoDTO> totalVendasPorMes();

        // 🔹 Lista dos anos com pedidos (para filtros)
        @Query("""
                        SELECT DISTINCT YEAR(p.dataCriacao)
                        FROM PedidoModel p
                        ORDER BY YEAR(p.dataCriacao) DESC
                        """)
        List<Integer> listarAnosPedidos();

        // 🔹 Projeção com filtro (mês e ano podem ser nulos)
        @Query("""
                        SELECT new com.projetoweb.dto.VendaMesProjecaoDTO(
                        MONTH(p.dataCriacao),
                        SUM(p.total)
                        )
                        FROM PedidoModel p
                        WHERE (:mes IS NULL OR MONTH(p.dataCriacao) = :mes)
                        AND (:ano IS NULL OR YEAR(p.dataCriacao) = :ano)
                        GROUP BY MONTH(p.dataCriacao)
                        ORDER BY MONTH(p.dataCriacao)
                        """)
        List<VendaMesProjecaoDTO> vendasPorMesFiltro(
                        @Param("mes") Integer mes,
                        @Param("ano") Integer ano);

        @Query(value = "SELECT MONTH(p.data_criacao) AS mes, SUM(p.total) AS total "
                + "FROM Pedidos p "
                + "GROUP BY MONTH(p.data_criacao) "
                + "ORDER BY mes;", nativeQuery = true)
        List<Object[]> findVendasPorAnoEMes();
}
