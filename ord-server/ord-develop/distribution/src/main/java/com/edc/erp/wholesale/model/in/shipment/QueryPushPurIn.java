package com.edc.erp.wholesale.model.in.shipment;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryPushPurIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 18:37
 **/
@Data
public class QueryPushPurIn implements Serializable {
    private static final long serialVersionUID = -6216633721509703666L;

    private String beginTime;

    private String endTime;

    private String shipmentStatus;

    private String distributionType;

    private String bizOrgCode;

    private String executeTime;
}
