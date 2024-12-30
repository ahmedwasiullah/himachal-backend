package com.hibernate.NMS.himachal_NMS.websockets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hibernate.NMS.himachal_NMS.config.SpringContextProvider;
import com.hibernate.NMS.himachal_NMS.config.WebSocketCors;
import com.hibernate.NMS.himachal_NMS.dto.AllDistrictDto;
import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.repository.StateRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Tuple;
import jakarta.websocket.*;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Slf4j
@Component
@EnableWebSocket
@ServerEndpoint(value = "/dashboard/{district_name}",configurator = WebSocketCors.class)
public class DashboardDistrictWebSocket {



    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionsMessage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    private final StateRepository stateRepository;
    public DashboardDistrictWebSocket() {
        this.objectMapper = new ObjectMapper();
        ApplicationContext context= SpringContextProvider.getApplicationContext();
        this.stateRepository=context.getBean(StateRepository.class);

    }


//    @PostConstruct
//    public void init() {
//        // Perform any initialization logic if necessary
//        if (stateRepository == null) {
//            log.error("StateRepository is not injected properly.");
//        }
//    }


    @OnOpen
    public void onOpen(Session session, @PathParam("district_name") String districtName) {
        // Add the session and district name to the map
        sessions.put(session, districtName);
        sessionsMessage.put(session,"");
        System.out.println("New connection for district: " + districtName);
        sendData();
        //share the data first from here
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // Handle incoming messages from the client
        System.out.println("Received message: " + message);
        sessionsMessage.put(session,message);
        try {
            session.getBasicRemote().sendText("Echo: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        // Remove the session from the map
        String districtName = sessions.remove(session);
                        sessionsMessage.remove(session);
        System.out.println("Connection closed for district: " + districtName);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle errors
        throwable.printStackTrace();
    }



    // Broadcast a message to all sessions for a specific district
    public synchronized static void broadcastToDistrict(String districtName, String message,Session session) {
        try {
            session.getBasicRemote().sendText(message);
            log.info("Message sent to session " + session.getId());
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public ConcurrentHashMap<Session, String> getSessions() {
        return sessions;
    }


    public ConcurrentHashMap<Session, String> getSessionsMessage() {
        return sessionsMessage;
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }



    public synchronized void sendData(){
        sessions.forEach((session, district) -> {
            if(!sessionsMessage.get(session).equals("")){
                System.out.println(sessionsMessage.get(session)+" the message");
                DashboardDistrictWebSocket.broadcastToDistrict(district, sessionsMessage.get(session),session);
            }else {
                try {

                    Tuple data = stateRepository.getDistrictData(district);
                    DistrictData curr = new DistrictData(district, (long) data.get(0), (long) data.get(1));
                    List<HimachalDevice> logs=stateRepository.getAllDistrictLogs(district);
                    AllDistrictDto allData=new AllDistrictDto(curr,logs);
                    String jsonData = getObjectMapper().writeValueAsString(allData);
                    DashboardDistrictWebSocket.broadcastToDistrict(district, jsonData,session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
//    @OnOpen
//    public void onOpen(Session session) {
//        // Wait for the first message that contains the district name
//        session.addMessageHandler(String.class, message -> {
//            String districtName = extractDistrictName(message); // Parse the message to get the district name
//            sessions.put(session, districtName);
//            district = districtName;
//            System.out.println("New connection for district: " + districtName);
//        });
//    }
//
//    private String extractDistrictName(String message) {
//        // Logic to extract district name from the message
//        // For example, if the message is "district=Solan", you can split it
//        return message.split("=")[1];
//    }

}
