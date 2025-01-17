package com.hibernate.NMS.himachal_NMS.Service;

import com.hibernate.NMS.himachal_NMS.dto.DistrictData;
import com.hibernate.NMS.himachal_NMS.repository.HimachalDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateService {

    @Autowired
    private HimachalDeviceRepository stateRepository;

    private List<DistrictData> getAllStateData(){
        return null;
    }
}
