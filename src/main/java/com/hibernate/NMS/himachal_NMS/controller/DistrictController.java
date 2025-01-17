package com.hibernate.NMS.himachal_NMS.controller;


import com.hibernate.NMS.himachal_NMS.Service.DistrictService;
import com.hibernate.NMS.himachal_NMS.dto.AllDeviceDataDTO;
import com.hibernate.NMS.himachal_NMS.dto.ApiResponse;
import com.hibernate.NMS.himachal_NMS.dto.DayData;
import com.hibernate.NMS.himachal_NMS.dto.PerformanceDeviceData;
import com.hibernate.NMS.himachal_NMS.exceptions.ResourceNotFoundException;
import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.model.History;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@Slf4j
@RequestMapping("/fetch")
public class DistrictController {


    @Autowired
    private DistrictService districtService;

    @GetMapping(value = "/{district}", produces = "application/json")

    public ResponseEntity<AllDeviceDataDTO> getDeviceDataLogs(@PathVariable String district, @RequestParam String deviceName, @RequestParam Integer days){
        try {
            List<History> logs = districtService.getDeviceDataLogs(district, deviceName, days);
            PerformanceDeviceData performanceDeviceData = districtService.getDevicePerformance(district, deviceName, days);
            return ResponseEntity.ok(new AllDeviceDataDTO("success",logs,performanceDeviceData));

        }catch (ResourceNotFoundException e){
            AllDeviceDataDTO data=new AllDeviceDataDTO(e.getMessage(),null,null);
            return ResponseEntity.status(NOT_FOUND).body(data);
        }
     }

     @GetMapping(value = "/{district}/data")
     public ResponseEntity<ApiResponse> getDeviceMonthlyHistory(@PathVariable String district, @RequestParam String deviceName, @RequestParam(required = false)int year,@RequestParam(required = false)int month) {
        try {
            List<Map<String,Object>> monthlyData=districtService.getDeviceMonthlyData(district,deviceName,year,month);
            return ResponseEntity.ok().body(new ApiResponse(monthlyData,"success"));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(null,"failed"));
        }


    }





     }
