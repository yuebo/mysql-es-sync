package com.eappcat.sync.es.entity;

import com.eappcat.sync.es.core.Text;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "database_sync")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DatabaseSyncEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Text
    private String name;
    private Date syncDate;
    @LastModifiedDate
    private Date updatedTime;

}
