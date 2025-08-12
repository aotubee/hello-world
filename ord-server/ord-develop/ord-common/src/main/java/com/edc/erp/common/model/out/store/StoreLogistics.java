package com.edc.erp.common.model.out.store;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 门店配送信息(ScStoreLogistics)实体类
 *
 * @author fxw
 * @since 2022-10-19 15:32:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "StoreLogistics", description = "门店配送信息")
public class StoreLogistics implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 配送id */
    @ApiModelProperty(name = "logisticsId", value = "配送id")
    private Integer logisticsId;

    /** 门店id */
    @ApiModelProperty(name = "storeId", value = "门店id")
    private Integer storeId;

    /** 常温商品配送周期 */
    @ApiModelProperty(name = "roomDistributionCycle", value = "常温商品配送周期")
    private String roomDistributionCycle;

    /** 常温门店收货窗口 */
    @ApiModelProperty(name = "roomReceivingWindow", value = "常温门店收货窗口")
    private String roomReceivingWindow;

    /** 低温商品配送周期 */
    @ApiModelProperty(name = "lowDistributionCycle", value = "低温商品配送周期")
    private String lowDistributionCycle;

    /** 低温门店收货窗口 */
    @ApiModelProperty(name = "lowReceivingWindow", value = "低温门店收货窗口")
    private String lowReceivingWindow;

    /** 冷冻商品配送周期 */
    @ApiModelProperty(name = "frozenDistributionCycle", value = "冷冻商品配送周期")
    private String frozenDistributionCycle;

    /** 冷冻门店收货窗口 */
    @ApiModelProperty(name = "frozenReceivingWindow", value = "冷冻门店收货窗口")
    private String frozenReceivingWindow;

    /** 常温配货优先级 */
    @ApiModelProperty(name = "roomDistributionPriority", value = "常温配货优先级")
    private Integer roomDistributionPriority;

    /** 冷冻配货优先级 */
    @ApiModelProperty(name = "frozenDistributionPriority", value = "冷冻配货优先级")
    private Integer frozenDistributionPriority;

    /** 常温日配周期，以英文逗号分隔 */
    @ApiModelProperty(name = "roomDeliveryDailyCycle", value = "常温日配周期，以英文逗号分隔")
    private String roomDeliveryDailyCycle;

    /** 低温日配周期，以英文逗号分隔 */
    @ApiModelProperty(name = "lowDeliveryDailyCycle", value = "低温日配周期，以英文逗号分隔")
    private String lowDeliveryDailyCycle;

    /** 冷冻日配周期，以英文逗号分隔 */
    @ApiModelProperty(name = "frozenDeliveryDailyCycle", value = "冷冻日配周期，以英文逗号分隔")
    private String frozenDeliveryDailyCycle;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改者 */
    @ApiModelProperty(name = "updater", value = "修改者")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /** 常温配货优先级 */
    @ApiModelProperty(name = "roomDistPry", value = "常温配货优先级")
    private Integer roomDistPry;

    /** 冷冻配货优先级 */
    @ApiModelProperty(name = "frozenDistPry", value = "冷冻配货优先级")
    private Integer frozenDistPry;

}
