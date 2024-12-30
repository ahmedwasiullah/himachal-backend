package com.hibernate.NMS.himachal_NMS.dto;

import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllDeviceDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    String Message;
    List<HimachalDevice> logs;
    PerformanceDeviceData performanceDeviceData;
}
