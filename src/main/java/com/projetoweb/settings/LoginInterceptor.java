package com.projetoweb.settings;

import org.springframework.web.servlet.HandlerInterceptor;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.UsuarioModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // se for recurso público, permite
        if (uri.startsWith("/login") || uri.startsWith("/cadastro") || uri.startsWith("/public")
                || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") || uri.equals("/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        UsuarioModel u = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (u == null) {
            response.sendRedirect("/login");
            return false;
        }

        // se admin e senha temporária, forçar mudança de senha (exceção: própria rota de alteração)
        if (u.getNivelAcesso() == NivelAcessoEnum.ADMINISTRADOR && u.getPrimeiroAcesso()) {
            if (!uri.startsWith("/usuario/alterar-senha") && !uri.startsWith("/logout")) {
                response.sendRedirect("/usuario/alterar-senha");
                return false;
            }
        }

        return true;
    }

}
