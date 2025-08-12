package com.edc.erp.directly.model.out;

import lombok.Data;

/**
 * @return: 订单配送周期出参
 * @Author: fxw
 * @Date: 2022/11/23
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
