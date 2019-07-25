package com.eappcat.sync.es.controller;

import com.eappcat.sync.es.dao.DeviceRepository;
import com.eappcat.sync.es.dao.EsDeviceRepository;
import com.eappcat.sync.es.entity.Device;
import com.eappcat.sync.es.entity.EsDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("device")
public class DeviceController {
    @Autowired
    private EsDeviceRepository repository;
    @GetMapping
    public List<EsDevice> list(){
        return repository.findAll(PageRequest.of(0,1000)).getContent();
    }

}
