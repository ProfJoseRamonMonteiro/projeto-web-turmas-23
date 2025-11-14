package com.projetoweb.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.projetoweb.dto.VendaMesDTO;
import com.projetoweb.models.ItemPedidoModel;
import com.projetoweb.models.PedidoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.repositories.ItemPedidoRepo;
import com.projetoweb.repositories.PedidoRepo;
import com.projetoweb.repositories.ProdutoRepo;

import jakarta.transaction.Transactional;

@Service
public class PedidoService {

    private final PedidoRepo pedidoRepository;
    private final ProdutoRepo produtoRepository;

    public PedidoService(PedidoRepo pedidoRepository,
                        ProdutoRepo produtoRepository,
                        ItemPedidoRepo itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public PedidoModel criarPedido(UsuarioModel cliente, List<ItemPedidoModel> itens) {
        if (cliente == null) throw new IllegalArgumentException("Cliente inválido");

        PedidoModel pedido = new PedidoModel();
        pedido.setCliente(cliente);
        pedido.setStatus("NEW");

        List<ItemPedidoModel> itensPersist = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoModel ip : itens) {
            ProdutoModel p = produtoRepository.findById(ip.getProduto().getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + ip.getProduto().getIdProduto()));

            if (p.getEstoque() < ip.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para produto: " + p.getNome());
            }

            // reduzir estoque
            p.setEstoque(p.getEstoque() - ip.getQuantidade());
            produtoRepository.save(p);

            ip.setPrecoUnitario(p.getPreco());
            ip.setPedido(pedido);
            itensPersist.add(ip);

            BigDecimal itemTotal = p.getPreco().multiply(BigDecimal.valueOf(ip.getQuantidade()));
            total = total.add(itemTotal);
        }

        pedido.setItens(itensPersist);
        pedido.setTotal(total);

        PedidoModel salvo = pedidoRepository.save(pedido);
        return salvo;
    }

    public List<VendaMesDTO> vendasPorMes() {
        List<Object[]> rows = pedidoRepository.findVendasPorAnoEMes();
        List<VendaMesDTO> resultado = new ArrayList<>();
        for (Object[] row : rows) {
            int mes = ((Number) row[0]).intValue();
            BigDecimal total = (BigDecimal) row[1];
            resultado.add(new VendaMesDTO(mes, total));
        }
        return resultado;
    }

}
