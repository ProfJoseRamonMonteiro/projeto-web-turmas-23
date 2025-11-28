package com.projetoweb.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projetoweb.dto.AvaliacaoDTO;
import com.projetoweb.models.AvaliacaoModel;
import com.projetoweb.repositories.AvaliacaoRepo;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepo avaliacaoRepository;

    public AvaliacaoModel salvar(AvaliacaoModel avaliacao) {

        Long idCliente = avaliacao.getCliente().getIdUsuario();
        Long idProduto = avaliacao.getProduto().getIdProduto();

        if (avaliacaoRepository.existsByClienteIdUsuarioAndProdutoIdProduto(idCliente, idProduto)) {
            throw new RuntimeException("O usuário já avaliou este produto.");
        }

        return avaliacaoRepository.save(avaliacao);
    }

    public List<AvaliacaoDTO> listarPorProduto(Long idProduto) {
        return avaliacaoRepository
                .findByProdutoIdProdutoOrderByDataAvaliacaoDesc(idProduto)
                .stream()
                .map(a -> new AvaliacaoDTO(
                        a.getCliente().getNome(),
                        a.getEstrelas(),
                        a.getComentario(),
                        a.getDataAvaliacao()
                ))
                .toList();
    }

    public double mediaAvaliacoes(Long idProduto) {
        return avaliacaoRepository
                .findByProdutoIdProdutoOrderByDataAvaliacaoDesc(idProduto)
                .stream()
                .mapToInt(AvaliacaoModel::getEstrelas)
                .average()
                .orElse(0);
    }

    public Map<Integer, Long> distribuicaoEstrelas(Long idProduto) {

        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);

        avaliacaoRepository.findByProdutoIdProdutoOrderByDataAvaliacaoDesc(idProduto)
                .forEach(a -> dist.put(a.getEstrelas(), dist.get(a.getEstrelas()) + 1));

        return dist;
    }

}