package com.projetoweb.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projetoweb.dto.ItemPedidoDTO;
import com.projetoweb.models.PedidoModel;
import com.projetoweb.repositories.ItemPedidoRepo;
import com.projetoweb.repositories.PedidoRepo;

@Service
public class ItemPedidoService {

        private final ItemPedidoRepo itemPedidoRepository;
        private final PedidoRepo pedidoRepository;

        public ItemPedidoService(ItemPedidoRepo itemPedidoRepository, PedidoRepo pedidoRepository) {
                this.itemPedidoRepository = itemPedidoRepository;
                this.pedidoRepository = pedidoRepository;
        }

        public List<ItemPedidoDTO> buscarItensPorPedido(Long idPedido) {

                PedidoModel pedido = pedidoRepository.findById(idPedido)
                                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + idPedido));

                return itemPedidoRepository.findByPedido(pedido)
                                .stream()
                                .map(item -> new ItemPedidoDTO(
                                                item.getProduto().getNome(),
                                                item.getQuantidade(),
                                                item.getPrecoUnitario()))
                                .toList();
        }
}