package com.hibernate.NMS.himachal_NMS.websockets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibernate.NMS.himachal_NMS.config.SpringContextProvider;
import com.hibernate.NMS.himachal_NMS.config.WebSocketCors;
import com.hibernate.NMS.himachal_NMS.dto.AllDistrictDto;
import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.enums.Status;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.repository.HimachalDeviceRepository;
import jakarta.websocket.*;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableWebSocket
@ServerEndpoint(value = "/dashboard/{district_name}",configurator = WebSocketCors.class)
public class DashboardDistrictWebSocket {



    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionsMessage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    private final HimachalDeviceRepository stateRepository;
    public DashboardDistrictWebSocket() {
        this.objectMapper = new ObjectMapper();
        ApplicationContext context= SpringContextProvider.getApplicationContext();
        this.stateRepository=context.getBean(HimachalDeviceRepository.class);

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
        try {
            sessions.put(session, districtName);
            sessionsMessage.put(session, "");
            System.out.println("New connection for district: " + districtName);
            sendData();
        }catch (Exception e){
            log.error(e.getMessage());
        }
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
            log.error(e.getMessage());
        } catch (Exception e){
            log.error(e.getMessage()!=null?e.getMessage():"null pointer");
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
//        log.error(throwable.printStackTrace());
    }



    // Broadcast a message to all sessions for a specific district
    public synchronized static void broadcastToDistrict(String districtName, String message,Session session) {
        try {
            session.getBasicRemote().sendText(message);
            log.info("Message sent to session " + session.getId());
        } catch (IOException e) {
             log.error(e.getMessage());
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
            try {
              if(!sessionsMessage.get(session).equals("")){

                System.out.println(sessionsMessage.get(session)+" the message");
                DashboardDistrictWebSocket.broadcastToDistrict(district, sessionsMessage.get(session),session);
              }else {

                List<HimachalDevice> logs;
                if (district.equalsIgnoreCase("Virtual Machine")) {
                    logs = stateRepository.getVMDataAndLogs();
                } else if (district.equalsIgnoreCase("Control room")) {
                    logs = stateRepository.getDistrictDataAndLogs("SEOCHP");
                } else if (district.contains("Lahaul")) {
                    logs = stateRepository.getDistrictDataAndLogs("LAHAUL");
                } else {
                    logs = stateRepository.getDistrictDataAndLogs(district);
                }

                int active = 0;
                int inactive = 0;
                for (HimachalDevice log : logs) {
                    if (log.getStatus().equals(Status.DOWN)) {
                        inactive++;
                    } else active++;
                }
                DistrictData curr = new DistrictData(district, active, inactive);
                AllDistrictDto allData = new AllDistrictDto(curr, logs);
                String jsonData = getObjectMapper().writeValueAsString(allData);
                DashboardDistrictWebSocket.broadcastToDistrict(district, jsonData, session);

              }
            } catch (Exception e) {
                log.error("session not available");
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
