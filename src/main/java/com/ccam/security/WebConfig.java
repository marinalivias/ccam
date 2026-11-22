package com.ccam.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final AlunoInterceptor alunoInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, AlunoInterceptor alunoInterceptor) {
        this.authInterceptor = authInterceptor;
        this.alunoInterceptor = alunoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/app/**");
        registry.addInterceptor(alunoInterceptor).addPathPatterns("/api/app/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/app", "/app/");
        registry.addViewController("/app/").setViewName("forward:/app/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/exercicios/**").allowedOrigins("*").allowedMethods("GET", "HEAD");
    }
}
