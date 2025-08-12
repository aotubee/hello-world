/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * StoreLogisticsOut返回类
 * @author lishaobo
 */
@Data
public class StoreLogisticsOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送id")
    private Integer logisticsId;

    @ApiModelProperty(value = "门店id")
    private Integer storeId;

    /**
     * 常温商品配送周期
     */
    @ApiModelProperty(name = "roomDistributionCycle", value = "常温商品配送周期")
    private String roomDistributionCycle;

    /**
     * 常温商品配送周期
     */
    @ApiModelProperty(name = "roomDistributionCycleStr", value = "常温商品配送周期中文")
    private String roomDistributionCycleStr;

    /**
     * 常温门店收货窗口
     */
    @ApiModelProperty(name = "roomReceivingWindow", value = "常温门店收货窗口")
    private String roomReceivingWindow;

    /**
     * 低温商品配送周期
     */
    @ApiModelProperty(name = "lowDistributionCycle", value = "低温商品配送周期")
    private String lowDistributionCycle;

    /**
     * 低温商品配送周期
     */
    @ApiModelProperty(name = "lowDistributionCycleStr", value = "低温商品配送周期中文")
    private String lowDistributionCycleStr;

    /**
     * 低温门店收货窗口
     */
    @ApiModelProperty(name = "lowReceivingWindow", value = "低温门店收货窗口")
    private String lowReceivingWindow;

    /**
     * 冷冻商品配送周期
     */
    @ApiModelProperty(name = "frozenDistributionCycle", value = "冷冻商品配送周期")
    private String frozenDistributionCycle;

    /**
     * 冷冻商品配送周期
     */
    @ApiModelProperty(name = "frozenDistributionCycleStr", value = "冷冻商品配送周期中文")
    private String frozenDistributionCycleStr;

    /**
     * 冷冻门店收货窗口
     */
    @ApiModelProperty(name = "frozenReceivingWindow", value = "冷冻门店收货窗口")
    private String frozenReceivingWindow;

    /**
     * 常温日配周期
     */
    @ApiModelProperty(name = "roomDeliveryDailyCycle", value = "常温日配周期")
    private String roomDeliveryDailyCycle;
    /**
     * 低温日配周期
     */
    @ApiModelProperty(name = "lowDeliveryDailyCycle", value = "低温日配周期")
    private String lowDeliveryDailyCycle;

    /**
     * 冷冻日配周期
     */
    @ApiModelProperty(name = "frozenDeliveryDailyCycle", value = "冷冻日配周期")
    private String frozenDeliveryDailyCycle;


    @ApiModelProperty(value = "常温配货优先级")
    private Integer roomDistPry;

    @ApiModelProperty(value = "冷冻配货优先级")
    private Integer frozenDistPry;
}
