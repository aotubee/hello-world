package com.edc.erp.common.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lishaobo
 * @date 2023-1-6
 */
@Data
public class ActivityRegulationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则主键 */
    @ApiModelProperty(name = "id", value = "规则主键")
    private Integer id;

    /** 活动商品特价主键 */
    @ApiModelProperty(name = "activityGoodsSpecialId", value = "活动商品特价主键")
    private Integer activityGoodsSpecialId;

    /** 下限商品数 */
    @ApiModelProperty(name = "downQuantity", value = "下限商品数")
    private Integer downQuantity;

    /** 上限商品数 */
    @ApiModelProperty(name = "upQuantity", value = "上限商品数")
    private Integer upQuantity;

    /** 折扣 */
    @ApiModelProperty(name = "dis", value = "折扣")
    private BigDecimal dis;

    /** 促销价 */
    @ApiModelProperty(name = "promotionPrice", value = "促销价")
    private BigDecimal promotionPrice;
}
