package com.edc.erp.model.out;

import lombok.Data;

/**
 *
 * @author wei
 */
@Data
public class OrderCycleDeliveryOut {
    /**
     * 配送周期
     */
    private String distributionCycle;
    /**
     * 日配周期
     */
    private String deliveryDailyCycle;
}
