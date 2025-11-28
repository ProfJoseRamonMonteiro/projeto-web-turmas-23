package com.projetoweb.dao;

public class CarrinhoItem {

    private Long idProduto;
    private Integer quantidade;

    // Construtores
    public CarrinhoItem() { }

    public CarrinhoItem(Long idProduto, Integer quantidade) {
        this.idProduto = idProduto;
        this.quantidade = quantidade;
    }

    // Getters e Setters
    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
