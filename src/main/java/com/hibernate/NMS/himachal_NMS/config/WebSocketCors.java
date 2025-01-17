package com.hibernate.NMS.himachal_NMS.config;


import jakarta.websocket.server.ServerEndpointConfig;


public class WebSocketCors extends ServerEndpointConfig.Configurator {

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        // Allow specific frontend origins
        return true; // Replace with your frontend origin
    }
}
