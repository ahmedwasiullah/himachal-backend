package com.hibernate.NMS.himachal_NMS.config;


import com.hibernate.NMS.himachal_NMS.repository.StateRepository;
import com.hibernate.NMS.himachal_NMS.websockets.DashboardStateWebSocket;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class WebSocketCors extends ServerEndpointConfig.Configurator {

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        // Allow specific frontend origins
        return true; // Replace with your frontend origin
    }
}
