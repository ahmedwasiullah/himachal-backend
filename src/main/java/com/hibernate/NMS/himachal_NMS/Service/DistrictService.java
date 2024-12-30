package com.hibernate.NMS.himachal_NMS.Service;

import com.hibernate.NMS.himachal_NMS.dto.PerformanceDeviceData;
import com.hibernate.NMS.himachal_NMS.exceptions.ResourceNotFoundException;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.repository.StateRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class DistrictService {

    @Autowired
    private StateRepository stateRepository;

    public List<HimachalDevice> getDeviceDataLogs(String district, String deviceName, Integer days) throws ResourceNotFoundException{
              if(stateRepository.existsByDistrictAndName(district,deviceName)){
                  return stateRepository.getDataByDistrictAndDeviceNameOfDays(district,deviceName,days);
              }else{
                  throw new ResourceNotFoundException("Device not found");
              }
    }

    public PerformanceDeviceData getDevicePerformance(String district, String deviceName, Integer days) {
             Tuple tuple =stateRepository.getPerformanceDataByDeviceOfDays(district,deviceName,days);
        return new PerformanceDeviceData((Long) tuple.get(0),(Long) tuple.get(1),new BigDecimal(tuple.get(2).toString()),new BigDecimal(tuple.get(3).toString()));
    }
}
