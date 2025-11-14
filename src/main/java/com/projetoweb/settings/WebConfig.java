package com.projetoweb.settings;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor geral para verificar sessão ativa (aplica a todas rotas exceto
        // públicas)
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/**", "/login", "/logout", "/cadastro", "image/**", "/dist/**", "/css/**", "/js/**",
                        "/images/**", "/webjars/**");

        // Interceptor específico para /admin/**
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**");
    }
}
