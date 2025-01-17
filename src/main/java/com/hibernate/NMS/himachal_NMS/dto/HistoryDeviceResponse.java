package com.hibernate.NMS.himachal_NMS.dto;

import com.hibernate.NMS.himachal_NMS.enums.Status;
import java.util.Date;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDeviceResponse {

    private String name;

    private String ip;


    private Status status;

    private String district;

    private Date timestamp;

    private Double inactive_hrs;
}
