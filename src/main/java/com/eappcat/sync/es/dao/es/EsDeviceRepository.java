package com.eappcat.sync.es.dao.es;

import com.eappcat.sync.es.entity.Device;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface EsDeviceRepository extends ElasticsearchCrudRepository<Device,Long> {
}
