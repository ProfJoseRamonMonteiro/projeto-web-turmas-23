package com.projetoweb.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "avaliacoes", uniqueConstraints = @UniqueConstraint(columnNames = { "id_usuario", "id_produto" }))
public class AvaliacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvaliacao;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    private Integer estrelas;

    private LocalDateTime dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuarioModel cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto")
    private ProdutoModel produto;

    @PrePersist
    public void prePersist() {
        dataAvaliacao = LocalDateTime.now();
    }

    // GETTERS E SETTERS
    public Long getIdAvaliacao() {
        return idAvaliacao;
    }

    public void setIdAvaliacao(Long idAvaliacao) {
        this.idAvaliacao = idAvaliacao;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getEstrelas() {
        return estrelas;
    }

    public void setEstrelas(Integer estrelas) {
        this.estrelas = estrelas;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public UsuarioModel getCliente() {
        return cliente;
    }

    public void setCliente(UsuarioModel cliente) {
        this.cliente = cliente;
    }

    public ProdutoModel getProduto() {
        return produto;
    }

    public void setProduto(ProdutoModel produto) {
        this.produto = produto;
    }

}
