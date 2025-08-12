package com.edc.erp.wholesale.model.in;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName PushPurShipmentIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/1/22 12:08
 **/
@Data
public class PushPurShipmentIn implements Serializable {
    private static final long serialVersionUID = 4900164777029287251L;

    private List<Long> shipmentIdList;

    private String bizOrgCode;
}
