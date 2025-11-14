package com.projetoweb.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projetoweb.repositories.PedidoRepo;

@Service
public class VendaService {

    @Autowired
    private PedidoRepo pedidoRepo;

    public Map<String, Double> obterTotaisMensais() {
        List<Object[]> resultados = pedidoRepo.findVendasPorAnoEMes();

        System.out.println(resultados.toString());

        Map<String, Double> totais = new LinkedHashMap<>();

        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                        "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        // inicializa todos os meses com 0
        for (String mes : meses) {
            totais.put(mes, 0.0);
        }

        for (Object[] linha : resultados) {
            Integer mes = ((Number) linha[0]).intValue();
            Double total = ((Number) linha[1]).doubleValue();
            totais.put(meses[mes - 1], total);
        }

        return totais;
    }

}
