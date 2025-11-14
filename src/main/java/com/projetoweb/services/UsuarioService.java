package com.projetoweb.services;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.UsuarioModel;
import com.projetoweb.repositories.UsuarioRepo;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepo usuarioRepository;
    // senha default para criação automatica do admin - deve ser alterada no primeiro acesso
    private static final String ADMIN_DEFAULT_USERNAME = "admin";
    private static final String ADMIN_DEFAULT_PASSWORD = "admin123!"; // recomende trocar em produção

    public UsuarioService(UsuarioRepo usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Cripta senha com BCrypt
    public String hashSenha(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }

    public boolean verificarSenha(String senhaPlain, String hash) {
        return BCrypt.checkpw(senhaPlain, hash);
    }

    public Optional<UsuarioModel> findByNomeUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuario(nomeUsuario);
    }

    @Transactional
    public UsuarioModel registrarCliente(String nomeUsuario, String senhaPlain, String nome) {
        if (usuarioRepository.existsByNomeUsuario(nomeUsuario)) {
            throw new IllegalArgumentException("Nome de usuário já existe");
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

    @Transactional
    public void initAdminIfNotExists() {
        if (!usuarioRepository.existsByNomeUsuario(ADMIN_DEFAULT_USERNAME)) {
            UsuarioModel admin = new UsuarioModel();
            admin.setNomeUsuario(ADMIN_DEFAULT_USERNAME);
            admin.setNome("Administrador do Sistema");
            admin.setSenha(hashSenha(ADMIN_DEFAULT_PASSWORD));
            admin.setNivelAcesso(NivelAcessoEnum.ADMINISTRADOR);
            admin.setHabilitado(true);
            admin.setPrimeiroAcesso(true); // forçar alteração no primeiro login
            usuarioRepository.save(admin);
        }
    }

    @Transactional
    public UsuarioModel alterarSenha(Long usuarioId, String novaSenha) {
        UsuarioModel u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        u.setSenha(hashSenha(novaSenha));
        u.setPrimeiroAcesso(false);
        return usuarioRepository.save(u);
    }

    // Método para validação manual de login (sem Spring Security)
    public Optional<UsuarioModel> autenticar(String username, String senhaPlain) {
        System.out.println("PegaUser1");
        Optional<UsuarioModel> ou = usuarioRepository.findByNomeUsuario(username);
        if (ou.isPresent()) {
            UsuarioModel u = ou.get();
            if (verificarSenha(senhaPlain, u.getSenha())) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

}
