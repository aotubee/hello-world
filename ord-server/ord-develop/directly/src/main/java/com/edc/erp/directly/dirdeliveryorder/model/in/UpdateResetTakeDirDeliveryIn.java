package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-11-05 09:55
 */
@Data
public class UpdateResetTakeDirDeliveryIn extends BaseEntity {

    private Integer receiveProgress;

    private Integer heartRate;

    private Long dirDeliveryOrderId;

    private String updater;

    private String deliveryStatusCode;

    private String bizOrgCode;
}
