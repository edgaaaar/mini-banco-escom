package com.escom.banco.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFiltroManual> loggingFilter(){
        FilterRegistrationBean<JwtFiltroManual> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new JwtFiltroManual());
        registrationBean.addUrlPatterns("/api/*");
        
        return registrationBean;
    }
}