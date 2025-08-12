package com.edc.erp.wholesale.model.in;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName PushPurUpdateIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/6/20 9:09
 **/
@Data
public class PushPurUpdateIn implements Serializable {
    private static final long serialVersionUID = 284889141518593386L;

    private List<Long> shipmentIdList;
    private String purBatchNumber;
    private String updater;
    private LocalDateTime updateTime;
    private Integer pushPurProgress;
}
