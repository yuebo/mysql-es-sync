package com.eappcat.sync.es.entity;

import com.eappcat.sync.es.core.SyncEntityListener;
import com.eappcat.sync.es.core.Text;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "tbl_device")
@Data
@EntityListeners({AuditingEntityListener.class,SyncEntityListener.class})
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @LastModifiedDate
    private Date updatedTime;
    @Text
    private String deviceName;
    private String deviceCode;
    private String gbId;
    private String deviceType;
    private BigDecimal lat;
    private BigDecimal lng;
}
