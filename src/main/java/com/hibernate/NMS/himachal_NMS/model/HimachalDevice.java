package com.hibernate.NMS.himachal_NMS.model;

import com.hibernate.NMS.himachal_NMS.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "himachal")
public class HimachalDevice {
    @Id
    private Integer id;

    private String name;

    private String ip;

    @Enumerated(EnumType.STRING)
    private Status status;


    private Date timestamp;

    private String district;
}
