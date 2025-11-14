package com.projetoweb.settings;

import org.springframework.web.servlet.HandlerInterceptor;

import com.projetoweb.models.NivelAcessoEnum;
import com.projetoweb.models.UsuarioModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        UsuarioModel u = (UsuarioModel) session.getAttribute("usuarioLogado");
        if (u == null || u.getNivelAcesso() != NivelAcessoEnum.ADMINISTRADOR) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }

}
