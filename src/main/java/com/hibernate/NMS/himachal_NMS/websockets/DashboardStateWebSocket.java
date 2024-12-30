package com.hibernate.NMS.himachal_NMS.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibernate.NMS.himachal_NMS.config.SpringContextProvider;
import com.hibernate.NMS.himachal_NMS.config.WebSocketCors;
import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.repository.StateRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Tuple;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ServerEndpoint(value = "/dashboard",configurator = WebSocketCors.class)
@Component

public class DashboardStateWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private Session session;


    private final ObjectMapper objectMapper;



    private final StateRepository stateRepository;
    public DashboardStateWebSocket() {
        this.objectMapper = new ObjectMapper();
        ApplicationContext context= SpringContextProvider.getApplicationContext();
        this.stateRepository=context.getBean(StateRepository.class);
    }

  public String message;



    public Set<Session> getSessions() {
        return sessions;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        synchronized (sessions) {
            sessions.add(session);
        }
        log.info(session.getId()+" session created");
        sendData();
        System.out.println("New connection: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        synchronized (sessions) {
            sessions.remove(session);
        }
        System.out.println("Closed connection: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
         log.info(message+" from the client with id "+session.getId());
          sendPeriodicUpdate("accepted request please proceed",session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error(throwable.getMessage());
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    public synchronized static void sendPeriodicUpdate(String data,Session connection) {
        if(connection.isOpen()) {
            try {

                connection.getBasicRemote().sendText(data);
                log.info("Message sent to session " + connection.getId());
            } catch (IOException e) {
                log.error("Error sending message to session " + connection.getId() + ": " + e.getMessage());
                // Optionally remove the session if it's in an invalid state

            }
        }


    }


    public synchronized void sendData(){
        sessions.forEach(session -> {
            try {
                List<Tuple> result=stateRepository.getAllData();
                List<DistrictData>data=result.stream().map(tuple -> {
                    String district = tuple.get("district", String.class);
                    Long activeDevices = tuple.get("active_devices", Long.class);
                    Long inactiveDevices = tuple.get("inactive_devices", Long.class);
                    return new DistrictData(district, activeDevices, inactiveDevices);
                }).collect(Collectors.toList());
                // Convert List<DistrictData> to JSON string

                String jsonData = objectMapper.writeValueAsString(data);
                // Send JSON data over WebSocket (you can call your WebSocket send method here)
                sendPeriodicUpdate(jsonData,session);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }


}
