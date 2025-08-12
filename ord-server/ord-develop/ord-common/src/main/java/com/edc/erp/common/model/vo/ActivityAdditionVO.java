package com.edc.erp.common.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lishaobo
 * @date 2023-1-6
 */
@Data
public class ActivityAdditionVO extends ActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动价
     */
    @ApiModelProperty(name = "activityPrice", value = "活动价")
    private BigDecimal activityPrice;

    /**
     * 活动名称
     */
    @ApiModelProperty(name = "activityName", value = "活动名称")
    private String activityName;

    /**
     * 活动数量
     */
    @ApiModelProperty(name = "activityNum", value = "活动数量")
    private BigDecimal activityNum;

    /**
     * 限量方式
     */
    @ApiModelProperty(name = "limitedWay", value = "限量方式")
    private String limitedWay;

    /**
     * 均摊价
     */
    @ApiModelProperty(name = "sharePrice", value = "均摊价")
    private BigDecimal sharePrice;

    /**
     * 特价活动分段规则
     */
    @ApiModelProperty(name = "activityRegulations", value = "特价活动分段规则")
    private List<ActivityRegulationVO> activityRegulations;

    /**
     * 活动单价
     */
    @ApiModelProperty(name = "activityUnitPrice", value = "活动单价")
    private BigDecimal activityUnitPrice;

    /** 活动类型名称(特价，赠品) */
    @ApiModelProperty(name = "activityTypeName", value = "活动类型名称(特价，赠品)")
    private String activityTypeName;

    /** 状态名称 */
    @ApiModelProperty(name = "statusName", value = "状态名称")
    private String statusName;

    /**
     * 限量方式名称
     */
    @ApiModelProperty(name = "limitedWayName", value = "限量方式名称")
    private String limitedWayName;

    /** 促销分组主键 */
    @ApiModelProperty(name = "promotionalActivityId", value = "促销分组主键")
    private Integer promotionalActivityId;

    /**
     * 活动赠品商品集合
     */
    @ApiModelProperty(name = "activityRegulations", value = "活动赠品商品集合")
    private List<ActivityGivingGoodsVO> givingGoodsList;

    /**
     * 活动其它主商品集合
     */
    @ApiModelProperty(name = "otherMainGoodsList", value = "活动其它主商品集合")
    private List<ActivityOtherMainGoodsVO> otherMainGoodsList;
}
