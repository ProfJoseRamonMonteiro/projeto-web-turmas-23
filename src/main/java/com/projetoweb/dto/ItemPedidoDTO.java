package com.projetoweb.dto;

import java.math.BigDecimal;

public class ItemPedidoDTO {

    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal preco;

    public ItemPedidoDTO(String nomeProduto, Integer quantidade, BigDecimal preco) {
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.preco = preco.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // getters e setters
    public BigDecimal getSubtotal() {
        return preco.multiply(BigDecimal.valueOf(quantidade));
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

}
