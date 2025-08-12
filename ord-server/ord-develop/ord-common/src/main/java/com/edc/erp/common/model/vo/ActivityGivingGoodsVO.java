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
public class ActivityGivingGoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主商品赠品组合主键 */
    @ApiModelProperty(name = "masterMerchandiseGiftCombinationId", value = "主商品赠品组合主键")
    private Integer masterMerchandiseGiftCombinationId;

    /** 促销主键 */
    @ApiModelProperty(name = "promotionalActivityId", value = "促销主键")
    private Integer promotionalActivityId;

    /** 商品代码 */
    @ApiModelProperty(name = "giftsCode", value = "商品代码")
    private String giftsCode;

    /** 赠品数量 */
    @ApiModelProperty(name = "giftsNum", value = "赠品数量")
    private Integer giftsNum;

    /** 赠品特价 */
    @ApiModelProperty(name = "giftsSpecialOffer", value = "赠品特价")
    private BigDecimal giftsSpecialOffer;

    /** 组合条件(并且，或者) */
    @ApiModelProperty(name = "combination", value = "组合条件(并且，或者)")
    private String combination;

    /** 赠品名称 */
    @ApiModelProperty(name = "goodsName", value = "赠品名称")
    private String goodsName;
}
