package com.eappcat.sync.es.core;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Date;
import java.util.stream.Stream;

/**
 * 根据updatedTime字段查询修改, 同步的Repository必须继承此接口
 * @param <T> 实体类
 */
@NoRepositoryBean
public interface SyncableRepository<T> {
    Stream<T> findByUpdatedTimeAfterAndUpdatedTimeBefore(Date start,Date end);
}
