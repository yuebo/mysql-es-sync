package com.eappcat.sync.es.dao;

import com.eappcat.sync.es.entity.EsDevice;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface EsDeviceRepository extends ElasticsearchCrudRepository<EsDevice,String> {
}
