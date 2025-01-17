package com.hibernate.NMS.himachal_NMS.websockets;

import com.hibernate.NMS.himachal_NMS.enums.Status;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.model.History;
import com.hibernate.NMS.himachal_NMS.repository.HistoryRepository;
import com.hibernate.NMS.himachal_NMS.repository.HimachalDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DashboardUpdateScheduler {

    @Autowired
    private HimachalDeviceRepository stateRepository;

    @Autowired
    private HistoryRepository historyRepository;


    @Autowired
    @Lazy
    private DashboardStateWebSocket dashboardStateWebSocket;

    @Scheduled(fixedRate = 10000) // Every 10sec
    public void sendUpdates() {
        dashboardStateWebSocket.sendData(new ArrayList<>(),new ArrayList<>());

    }


    @Autowired
    @Lazy
    private DashboardDistrictWebSocket dashboardDistrictWebSocket;

    @Scheduled(fixedRate = 60000)  //Every 1min
    public void sendDistrictUpdates() {
        dashboardDistrictWebSocket.sendData();
    }

    @Autowired
    private Map<String, String> districtMap;

    @Scheduled(fixedRate = 10000)
    public void compareDataCreateHistory(){
        //this code runs every six mintues and updates the history
                List<History>hptDevices=historyRepository.getLastStatus();
                List<HimachalDevice>hpDevices=stateRepository.getLastStatus();
                HashMap<String,History>hptMap=new HashMap<>();
                for(History device:hptDevices){
                    hptMap.put(device.getName(),device);
                }
                List<History>presentStatusList=new ArrayList<>();
        List<History>successList=new ArrayList<>();
        List<History>failureList=new ArrayList<>();
                for(HimachalDevice device:hpDevices){
                       if(hptMap.containsKey(device.getName())){
                           if(!device.getStatus().equals(hptMap.get(device.getName()).getStatus())){
                               String[]split=device.getName().split(" ");
                               String district="Virtual Machine";
                               if(districtMap.containsKey(split[0])) {
                                   district = districtMap.get(split[0]);
                               }
                               History updatedDev=History.builder().name(device.getName()).ip(device.getIp())
                                       .status(device.getStatus()).district(district)
                                       .timestamp(device.getTimestamp()).build();
                               presentStatusList.add(updatedDev);
                               if(device.getStatus().equals(Status.DOWN)){
                                   failureList.add(updatedDev);
                               }else{
                                   successList.add(updatedDev);
                               }
                           }
                       }else {
                           String[]split=device.getName().split(" ");
                           String district="Virtual Machine";
                           if(districtMap.containsKey(split[0])) {
                               district = districtMap.get(split[0]);
                           }
                           History updatedDev=History.builder().name(device.getName()).ip(device.getIp())
                                   .status(device.getStatus()).district(district).build();
                           presentStatusList.add(updatedDev);
                       }
                }
                if (!presentStatusList.isEmpty()){
                    historyRepository.saveAll(presentStatusList);
                    dashboardStateWebSocket.sendData(successList,failureList);
                }

    }
}



