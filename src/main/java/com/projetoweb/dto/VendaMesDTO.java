package com.projetoweb.dto;

import java.math.BigDecimal;

public class VendaMesDTO {

    private final Integer mes;
    private final String mesNome;      // nome abreviado (para gráficos)
    private final String mesNomeLongo; // nome completo (para filtros)
    private final BigDecimal total;

    public VendaMesDTO(Integer mes, BigDecimal total) {
        this.mes = mes;
        this.total = total;
        this.mesNome = nomeMesAbreviado(mes);
        this.mesNomeLongo = nomeMesLongo(mes);
    }

    private String nomeMesAbreviado(Integer m) {
        return switch (m) {
            case 1 -> "Jan";
            case 2 -> "Fev";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "Mai";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Set";
            case 10 -> "Out";
            case 11 -> "Nov";
            case 12 -> "Dez";
            default -> "";
        };
    }

    private String nomeMesLongo(Integer m) {
        return switch (m) {
            case 1 -> "Janeiro";
            case 2 -> "Fevereiro";
            case 3 -> "Março";
            case 4 -> "Abril";
            case 5 -> "Maio";
            case 6 -> "Junho";
            case 7 -> "Julho";
            case 8 -> "Agosto";
            case 9 -> "Setembro";
            case 10 -> "Outubro";
            case 11 -> "Novembro";
            case 12 -> "Dezembro";
            default -> "";
        };
    }

    public Integer getMes() { return mes; }
    public BigDecimal getTotal() { return total; }
    public String getMesNome() { return mesNome; }
    public String getMesNomeLongo() { return mesNomeLongo; }

}