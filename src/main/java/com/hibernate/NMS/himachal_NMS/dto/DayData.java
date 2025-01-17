package com.hibernate.NMS.himachal_NMS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DayData {

    private Date day;
    private BigDecimal activeHrs;
}
