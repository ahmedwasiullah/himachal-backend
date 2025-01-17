package com.hibernate.NMS.himachal_NMS.model;

import com.hibernate.NMS.himachal_NMS.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "hpt")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String ip;

    private Status status;

    @CurrentTimestamp
    private Date timestamp;

    private String district;
}
