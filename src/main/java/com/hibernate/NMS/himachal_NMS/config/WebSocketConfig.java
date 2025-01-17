package com.hibernate.NMS.himachal_NMS.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig {



    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


    @Bean
    public Map<String, String> districtMap() {
        Map<String, String> districtMap = new HashMap<>();
        districtMap.put("DDMASHIMLA", "Shimla");
        districtMap.put("DDMASOLAN", "Solan");
        districtMap.put("DEOCBILASPUR", "Bilaspur");
        districtMap.put("DEOCCHAMBA", "Chamba");
        districtMap.put("DEOCHAMIRPUR", "Hamirpur");
        districtMap.put("DEOCKANGRA", "Kangra");
        districtMap.put("DEOCKINNAUR", "Kinnaur");
        districtMap.put("DEOCKULLU", "Kullu");
        districtMap.put("DEOCLAHAUL&SPITI", "Lahaul & Spiti");
        districtMap.put("DEOCMANDI", "Mandi");
        districtMap.put("DEOCSIRMOUR", "Sirmour");
        districtMap.put("DEOCUNA", "Una");
        districtMap.put("Firewall", "Firewall");
        districtMap.put("SEOCHP", "Control Room");
        districtMap.put("VM", "Virtual Machine");
        return districtMap;
    }


}
