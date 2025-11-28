package com.projetoweb.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.repositories.UsuarioRepo;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepo usuarioRepository;

    private static final String ADMIN_DEFAULT_USERNAME = "admin";
    private static final String ADMIN_DEFAULT_PASSWORD = "admin123!";

    public UsuarioService(UsuarioRepo usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /*
     * ===========================
     * UTILIDADES
     * ===========================
     */
    private String hashSenha(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }

    public boolean verificarSenha(String senhaPlain, String hash) {
        return BCrypt.checkpw(senhaPlain, hash);
    }

    /*
     * ===========================
     * BUSCAS
     * ===========================
     */
    public Optional<UsuarioModel> findByNomeUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuario(nomeUsuario);
    }

    public Page<UsuarioModel> listarPorNivel(NivelAcessoEnum nivel, Pageable pg) {
        return usuarioRepository.findByNivelAcesso(nivel, pg);
    }

    /*
     * ===========================
     * REGISTRO DE CLIENTE
     * ===========================
     */
    @Transactional
    public UsuarioModel registrarCliente(String nomeUsuario, String senhaPlain, String nome) {

        if (usuarioRepository.existsByNomeUsuario(nomeUsuario)) {
            throw new IllegalArgumentException("Nome de usuário já está em uso.");
        }

        UsuarioModel u = new UsuarioModel();
        u.setNomeUsuario(nomeUsuario);
        u.setNome(nome);
        u.setSenha(hashSenha(senhaPlain));
        u.setNivelAcesso(NivelAcessoEnum.CLIENTE);
        u.setHabilitado(true);
        u.setPrimeiroAcesso(false);

        return usuarioRepository.save(u);
    }

    /*
     * ===========================
     * CRIAÇÃO DO ADMIN PADRÃO
     * ===========================
     */
    @Transactional
    public void initAdminIfNotExists() {
        if (!usuarioRepository.existsByNomeUsuario(ADMIN_DEFAULT_USERNAME)) {
            UsuarioModel admin = new UsuarioModel();
            admin.setNomeUsuario(ADMIN_DEFAULT_USERNAME);
            admin.setNome("Administrador do Sistema");
            admin.setSenha(hashSenha(ADMIN_DEFAULT_PASSWORD));
            admin.setNivelAcesso(NivelAcessoEnum.ADMINISTRADOR);
            admin.setHabilitado(true);
            admin.setPrimeiroAcesso(true);
            usuarioRepository.save(admin);
        }
    }

    /*
     * ===========================
     * ALTERAR SENHA
     * ===========================
     */
    @Transactional
    public UsuarioModel alterarSenha(Long usuarioId, String novaSenha) {
        UsuarioModel u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        u.setSenha(hashSenha(novaSenha));
        u.setPrimeiroAcesso(false);

        return usuarioRepository.save(u);
    }

    /*
     * ===========================
     * AUTENTICAÇÃO
     * ===========================
     */
    public Optional<UsuarioModel> autenticar(String username, String senhaPlain) {

        return usuarioRepository.findByNomeUsuario(username)
                .filter(u -> verificarSenha(senhaPlain, u.getSenha()));
    }

    /*
     * ===========================
     * ESTATÍSTICAS
     * ===========================
     */
    public Long totalClientes() {
        return usuarioRepository.countByNivelAcesso(NivelAcessoEnum.CLIENTE);
    }

    @Transactional
    public UsuarioModel toggleHabilitado(Long usuarioId) {
        UsuarioModel u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        u.setHabilitado(!u.isHabilitado());
        return usuarioRepository.save(u);
    }

}
