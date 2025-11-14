package com.projetoweb.dto;

import java.math.BigDecimal;

public class VendaMesDTO {

    private int mes;
    private BigDecimal total;

    public VendaMesDTO(int mes, BigDecimal total) {
        this.mes = mes;
        this.total = total;
    }

    // getters e setters
    public int getMes() { return mes; }
    public BigDecimal getTotal() { return total; }

}
