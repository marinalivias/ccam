package com.ccam.ccamv2.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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
}
