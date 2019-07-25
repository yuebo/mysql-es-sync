package com.eappcat.sync.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sync")
@Data
public class SyncConfigProperties {
    /**
     * 同步延时
     */
    private long delay=60000;
    /**
     * 同步时同步批量操作文档数量的大小
     */
    private long batchSize=5000;
    /**
     * 是否使用自动mapping. 如果使用spring data elasticsearch来管理index的话可以禁用此选项
     */
    private boolean autoMapping=true;
}
