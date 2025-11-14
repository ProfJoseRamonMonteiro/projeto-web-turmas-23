package com.projetoweb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.projetoweb.services.UsuarioService;

@Component
public class Inicializador implements CommandLineRunner{

    private final UsuarioService usuarioService;

    public Inicializador(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        System.out.println("PegaUser2");
    }

    public void run(String... args) {
        usuarioService.initAdminIfNotExists();
        System.out.println("PegaUser3");
    }

}
