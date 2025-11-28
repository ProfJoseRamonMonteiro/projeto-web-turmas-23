package com.projetoweb.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.projetoweb.dto.VendaMesDTO;
import com.projetoweb.dto.VendaMesProjecaoDTO;
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
    private final ItemPedidoRepo itemPedidoRepository;

    public PedidoService(PedidoRepo pedidoRepository,
            ProdutoRepo produtoRepository,
            ItemPedidoRepo itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public PedidoModel criarPedido(UsuarioModel cliente, List<ItemPedidoModel> itens) {

        if (cliente == null)
            throw new IllegalArgumentException("Cliente inválido");

        PedidoModel pedido = new PedidoModel();
        pedido.setCliente(cliente);
        pedido.setStatus("Novo");

        List<ItemPedidoModel> itensPersist = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoModel ip : itens) {

            ProdutoModel p = produtoRepository.findById(ip.getProduto().getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + ip.getProduto().getIdProduto()));

            if (p.getEstoque() < ip.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + p.getNome());
            }

            p.setEstoque(p.getEstoque() - ip.getQuantidade());
            produtoRepository.save(p);

            ip.setPrecoUnitario(p.getPreco());
            ip.setPedido(pedido);

            itensPersist.add(ip);

            total = total.add(p.getPreco().multiply(BigDecimal.valueOf(ip.getQuantidade())));
        }

        pedido.setItens(itensPersist);
        pedido.setTotal(total);

        PedidoModel pedidoSalvo = pedidoRepository.save(pedido);
        itemPedidoRepository.saveAll(itensPersist);

        return pedidoSalvo;
    }

    public BigDecimal faturamentoAnual() {
        return pedidoRepository.faturamentoAnual();
    }

    public Long totalPedidos() {
        return pedidoRepository.count();
    }

    public BigDecimal faturamentoMesAtual() {
        return pedidoRepository.faturamentoMesAtual();
    }

    // ==========================
    // AGORA COM DTO TIPO PROJECTION
    // ==========================
    public List<VendaMesDTO> vendasPorMes() {
        return pedidoRepository.totalVendasPorMes()
                .stream()
                .map(v -> new VendaMesDTO(v.mes(), v.total()))
                .toList();
    }

    public Map<Integer, String> getMesesMap() {
        Map<Integer, String> meses = new LinkedHashMap<>();
        meses.put(1, "Janeiro");
        meses.put(2, "Fevereiro");
        meses.put(3, "Março");
        meses.put(4, "Abril");
        meses.put(5, "Maio");
        meses.put(6, "Junho");
        meses.put(7, "Julho");
        meses.put(8, "Agosto");
        meses.put(9, "Setembro");
        meses.put(10, "Outubro");
        meses.put(11, "Novembro");
        meses.put(12, "Dezembro");
        return meses;
    }

    public List<Integer> getAnosDisponiveis() {
        return pedidoRepository.listarAnosPedidos();
    }

    // ================================
    // 🔥 FILTRO POR MÊS + ANO USANDO PROJECTION
    // ================================
    public List<VendaMesDTO> vendasPorMes(Integer mes, Integer ano) {

        List<VendaMesProjecaoDTO> resultados = pedidoRepository.vendasPorMesFiltro(mes, ano);

        Map<Integer, BigDecimal> mapa = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++)
            mapa.put(i, BigDecimal.ZERO);

        resultados.forEach(r -> mapa.put(r.mes(), r.total() != null ? r.total() : BigDecimal.ZERO));

        List<VendaMesDTO> lista = new ArrayList<>();

        mapa.forEach((m, total) -> {
            if (mes == null || mes.equals(m)) {
                lista.add(new VendaMesDTO(m, total));
            }
        });

        return lista;
    }

    public PedidoModel buscarPorId(Long idPedido) {
        return pedidoRepository.findById(idPedido).orElse(null);
    }

    public void enviarPedido(Long id) {

        PedidoModel pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatus("Enviado");

        pedidoRepository.save(pedido);
    }

}