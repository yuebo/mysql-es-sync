package com.eappcat.sync.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sync")
@Data
public class SyncConfigProperties {
    private long syncDelay=60000;
    private long batchSize=5000;
}
