package com.hibernate.NMS.himachal_NMS.controller;


import com.hibernate.NMS.himachal_NMS.Service.DistrictService;
import com.hibernate.NMS.himachal_NMS.dto.AllDeviceDataDTO;
import com.hibernate.NMS.himachal_NMS.dto.PerformanceDeviceData;
import com.hibernate.NMS.himachal_NMS.exceptions.ResourceNotFoundException;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@Slf4j
@RequestMapping("/fetch")
public class DistrictController {


    @Autowired
    private DistrictService districtService;

    @GetMapping(value = "/{district}", produces = "application/json")

    public ResponseEntity<AllDeviceDataDTO> getDeviceDataLogs(@PathVariable String district, @RequestParam String deviceName, @RequestParam(required = false) Integer days){
        try {
            List<HimachalDevice> logs = districtService.getDeviceDataLogs(district, deviceName, days);
            PerformanceDeviceData performanceDeviceData = districtService.getDevicePerformance(district, deviceName, days);
            return ResponseEntity.ok(new AllDeviceDataDTO("success",logs,performanceDeviceData));

        }catch (ResourceNotFoundException e){
            AllDeviceDataDTO data=new AllDeviceDataDTO(e.getMessage(),null,null);
            return ResponseEntity.status(NOT_FOUND).body(data);
        }
     }





}
