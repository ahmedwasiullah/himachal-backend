package com.hibernate.NMS.himachal_NMS.dto;

import com.hibernate.NMS.himachal_NMS.model.History;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParentDistrictAndHistoryDevice {
    private List<HistoryDeviceResponse>downLogs;
    private List<DistrictData>districtActivityLogs;
    private List<History>success;
    private List<History>failure;
}
