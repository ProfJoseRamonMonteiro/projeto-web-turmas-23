package com.projetoweb.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(unique = true, nullable = false)
    private String nomeUsuario;

    @Column(nullable = false)
    private String senha;

    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcessoEnum nivelAcesso;

    private boolean habilitado;
    private Boolean primeiroAcesso;

    // GETTERS E SETTERS

	public Long getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}
	public String getNomeUsuario() {
		return nomeUsuario;
	}
	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public NivelAcessoEnum getNivelAcesso() {
		return nivelAcesso;
	}
	public void setNivelAcesso(NivelAcessoEnum nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}
	public boolean isHabilitado() {
		return habilitado;
	}
	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}
	public Boolean getPrimeiroAcesso() {
		return primeiroAcesso;
	}
	public void setPrimeiroAcesso(Boolean primeiroAcesso) {
		this.primeiroAcesso = primeiroAcesso;
	}	

}