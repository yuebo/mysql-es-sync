package com.eappcat.sync.es.controller;

import com.eappcat.sync.es.core.RefreshMappingEvent;
import com.eappcat.sync.es.dao.jpa.DatabaseSyncRepository;
import com.eappcat.sync.es.entity.DatabaseSyncEntity;
import com.eappcat.sync.es.vo.DatabaseSyncVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("sync")
public class SyncController {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DatabaseSyncRepository repository;
    @PostMapping("add")
    public String add(@RequestBody @Validated DatabaseSyncVO databaseSyncVO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "500";
        }
        Optional<DatabaseSyncEntity> databaseSyncEntityOptional=repository.findByName(databaseSyncVO.getName());
        if(databaseSyncEntityOptional.isPresent()){
            return "400";
        }else {
            DatabaseSyncEntity syncEntity= new DatabaseSyncEntity();
            syncEntity.setName(databaseSyncVO.getName());
            syncEntity.setUpdatedTime(new Date());
            repository.save(syncEntity);
        }
        return "0";

    }
    @GetMapping("list")
    public List<DatabaseSyncEntity> list(){
        return repository.findAll();
    }

    @GetMapping("refresh")
    public String refresh(){
        applicationContext.publishEvent(new RefreshMappingEvent());
        return "0";
    }
}
