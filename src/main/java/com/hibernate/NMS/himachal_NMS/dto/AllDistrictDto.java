package com.hibernate.NMS.himachal_NMS.dto;

import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllDistrictDto {
    private DistrictData data;
    private List<HimachalDevice> log;
}
