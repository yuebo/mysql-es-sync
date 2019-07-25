package com.eappcat.sync.es.core;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * Entity监听器
 */
public class SyncEntityListener implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @PostPersist
    public void onPostPersist(Object entity){
        applicationContext.publishEvent(new EntityChangeEvent(EntityChangeEventType.INSERT,entity));

    }
    @PostUpdate
    public void onPostUpdate(Object entity){
        applicationContext.publishEvent(new EntityChangeEvent(EntityChangeEventType.UPDATE,entity));

    }

    @PostRemove
    public void onPostRemove(Object entity){
        applicationContext.publishEvent(new EntityChangeEvent(EntityChangeEventType.DELETE,entity));
    }


    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext=ctx;
    }
}
