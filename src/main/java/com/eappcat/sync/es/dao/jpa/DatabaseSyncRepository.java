package com.eappcat.sync.es.dao.jpa;

import com.eappcat.sync.es.entity.DatabaseSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatabaseSyncRepository extends JpaRepository<DatabaseSyncEntity,Long> {

    Optional<DatabaseSyncEntity> findByName(String name);
}

