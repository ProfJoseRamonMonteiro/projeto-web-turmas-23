package com.projetoweb.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.UsuarioModel;


public interface UsuarioRepo extends JpaRepository<UsuarioModel, Long>{
    
    Optional<UsuarioModel> findByNomeUsuario(String nomeUsuario);
    boolean existsByNomeUsuario(String nomeUsuario);
    long countByNivelAcesso(NivelAcessoEnum nivelAcesso);

Page<UsuarioModel> findByNivelAcesso(NivelAcessoEnum nivelAcesso, Pageable pageable);

}
