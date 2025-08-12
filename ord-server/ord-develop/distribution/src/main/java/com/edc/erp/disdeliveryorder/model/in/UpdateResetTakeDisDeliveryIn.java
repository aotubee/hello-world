package com.edc.erp.disdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-11-05 09:55
 */
@Data
public class UpdateResetTakeDisDeliveryIn extends BaseEntity {

    private Integer receiveProgress;

    private Integer heartRate;

    private Long disDeliveryOrderId;

    private String updater;

    private String deliveryStatusCode;

    private String bizOrgCode;
}
