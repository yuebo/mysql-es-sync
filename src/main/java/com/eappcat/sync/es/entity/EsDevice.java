package com.eappcat.sync.es.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Document(indexName = "tbl_device",type = "_doc",createIndex = false)
@Data
public class EsDevice {
    @Id
    private String id;
    private Date updatedTime;
    private String deviceName;
    private String deviceCode;
    private String gbId;
    private String deviceType;
    private BigDecimal lat;
    private BigDecimal lng;
}
