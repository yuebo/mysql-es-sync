package com.eappcat.sync.es.dao;

import com.eappcat.sync.es.core.SyncableRepository;
import com.eappcat.sync.es.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device,Long>, SyncableRepository<Device> {
}
