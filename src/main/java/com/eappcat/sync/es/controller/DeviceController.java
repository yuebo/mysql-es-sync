package com.eappcat.sync.es.controller;

import com.eappcat.sync.es.dao.es.EsDeviceRepository;
import com.eappcat.sync.es.entity.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("device")
public class DeviceController {
    @Autowired
    private EsDeviceRepository repository;
    @GetMapping
    public List<Device> list(){
        return repository.findAll(PageRequest.of(0,1000)).getContent();
    }

}
