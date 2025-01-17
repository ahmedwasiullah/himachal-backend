package com.hibernate.NMS.himachal_NMS.websockets;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibernate.NMS.himachal_NMS.config.SpringContextProvider;
import com.hibernate.NMS.himachal_NMS.config.WebSocketCors;
import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.dto.HistoryDeviceResponse;
import com.hibernate.NMS.himachal_NMS.dto.ParentDistrictAndHistoryDevice;
import com.hibernate.NMS.himachal_NMS.enums.Status;
import com.hibernate.NMS.himachal_NMS.model.History;
import com.hibernate.NMS.himachal_NMS.repository.HimachalDeviceRepository;
import com.hibernate.NMS.himachal_NMS.repository.HistoryRepository;
import jakarta.persistence.Tuple;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;



@Slf4j
@ServerEndpoint(value = "/dashboard",configurator = WebSocketCors.class)
@Component

public class DashboardStateWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private Session session;
    private final Map<String, String> districtMap;

    private final ObjectMapper objectMapper;

    private final HistoryRepository historyRepository;

    private final HimachalDeviceRepository stateRepository;
    public DashboardStateWebSocket() {
        this.objectMapper = new ObjectMapper();
        ApplicationContext context= SpringContextProvider.getApplicationContext();
        this.stateRepository=context.getBean(HimachalDeviceRepository.class);
        this.districtMap = context.getBean("districtMap", Map.class);
        this.historyRepository=context.getBean(HistoryRepository.class);
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
        sendData(new ArrayList<>(),new ArrayList<>());
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
        try {
            log.info(message+" from the client with id "+session.getId());
            sendPeriodicUpdate("accepted request please proceed",session);
        }catch (Exception e){
            log.error(e.getMessage()!=null?e.getMessage():"null pointer");
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {

        log.error(throwable.getMessage());
//        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    public synchronized static void sendPeriodicUpdate(String data,Session connection) {
        if (connection.isOpen()) {
            try {
                connection.getBasicRemote().sendText(data);
                log.info("Message sent to session " + connection.getId());
            } catch (IOException e) {
                log.error("Error sending message to session " + connection.getId() + ": " + e.getMessage(), e);
                // Optionally remove the session if it's in an invalid state
            }
        }


    }


    public synchronized void sendData(List<History>success, List<History>failure){
        sessions.forEach(session -> {
            try {
                List<Tuple> result=stateRepository.getAllData();
                List<DistrictData>data=result.stream().map(tuple -> {

                    String district = tuple.get("group_name", String.class);
                      district=districtMap.get(district);
                    Long activeDevices = tuple.get("active_devices", Long.class);
                    Long inactiveDevices = tuple.get("inactive_devices", Long.class);
                    return new DistrictData(district, activeDevices.intValue(), inactiveDevices.intValue());
                }).collect(Collectors.toList());
                // Convert List<DistrictData> to JSON string
                  List<History>logs=historyRepository.getLastDownStatusDevices();
                  List<HistoryDeviceResponse>logsData=logs.stream().filter(log->log.getStatus().equals(Status.DOWN))
                          .map(log->{
                         return HistoryDeviceResponse.builder().ip(log.getIp())
                                 .name(log.getName()).district(log.getDistrict())
                                 .timestamp(log.getTimestamp()).status(log.getStatus())
                                 .inactive_hrs(
                                         Duration.between(log.getTimestamp().toInstant().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime(),
                                                 ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))).getSeconds() *1d/3600
                                 ).build();
                   }
                  ).collect(Collectors.toList());



                ParentDistrictAndHistoryDevice finalData=new ParentDistrictAndHistoryDevice(logsData,data,success,failure);
                String jsonData = objectMapper.writeValueAsString(finalData);
                // Send JSON data over WebSocket (you can call your WebSocket send method here)
                sendPeriodicUpdate(jsonData,session);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            } catch (Exception e){
                log.error(e.getMessage()!=null?e.getMessage():"null pointer exception");
            }
        });
    }


}
