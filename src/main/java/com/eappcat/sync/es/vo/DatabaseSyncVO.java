package com.eappcat.sync.es.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DatabaseSyncVO {
    @NotEmpty(message = "名称不能为空")
    private String name;
}
