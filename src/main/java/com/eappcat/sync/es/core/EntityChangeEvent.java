package com.eappcat.sync.es.core;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity事件
 */
@Data
@AllArgsConstructor
public class EntityChangeEvent {
    private EntityChangeEventType type;
    private Object entity;
}
