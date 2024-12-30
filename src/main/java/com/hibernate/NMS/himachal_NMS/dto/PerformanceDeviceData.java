package com.hibernate.NMS.himachal_NMS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceDeviceData {

    Long ups;
    Long downs;
    BigDecimal upTime;
    BigDecimal downTime;
}
