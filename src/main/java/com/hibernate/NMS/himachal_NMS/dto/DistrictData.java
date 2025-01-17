package com.hibernate.NMS.himachal_NMS.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hibernate.NMS.himachal_NMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DistrictData {

    private String district;
    private int active_devices;
    private int inactive_devices;


}
