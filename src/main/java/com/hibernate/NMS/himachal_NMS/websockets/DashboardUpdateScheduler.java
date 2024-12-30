package com.hibernate.NMS.himachal_NMS.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibernate.NMS.himachal_NMS.dto.AllDistrictDto;
import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.repository.StateRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardUpdateScheduler {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    @Lazy
    private DashboardStateWebSocket dashboardStateWebSocket;

    @Scheduled(fixedRate = 10000) // Every 10sec
    public void sendUpdates() {
        dashboardStateWebSocket.sendData();

    }


    @Autowired
    @Lazy
    private DashboardDistrictWebSocket dashboardDistrictWebSocket;

    @Scheduled(fixedRate = 10000)  //Every 10sec
    public void sendDistrictUpdates() {
        dashboardDistrictWebSocket.sendData();
    }
}



