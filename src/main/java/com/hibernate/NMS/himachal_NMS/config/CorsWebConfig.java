package com.hibernate.NMS.himachal_NMS.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@EnableWebSocket
public class CorsWebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {


        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins) // Adjust this to your frontend's URL
                .allowedMethods("*").allowedHeaders("*");
    }




}



