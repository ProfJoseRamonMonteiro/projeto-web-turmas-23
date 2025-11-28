package com.projetoweb.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AvaliacaoDTO {

    private final String clienteNome;
    private final Integer estrelas;
    private final String comentario;
    private final String dataFormatada;
    private final String horaFormatada;

    public AvaliacaoDTO(String clienteNome, Integer estrelas, String comentario, LocalDateTime data) {
        this.clienteNome = clienteNome;
        this.estrelas = estrelas;
        this.comentario = comentario;

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("pt", "BR"));
        this.dataFormatada = data.format(df).toUpperCase();

        this.horaFormatada = data.format(DateTimeFormatter.ofPattern("HH:mm")) + "hs";
    }

    // Getters
    public String getClienteNome() {
        return clienteNome;
    }

    public Integer getEstrelas() {
        return estrelas;
    }

    public String getComentario() {
        return comentario;
    }

    public String getDataFormatada() {
        return dataFormatada;
    }

    public String getHoraFormatada() {
        return horaFormatada;
    }

}
