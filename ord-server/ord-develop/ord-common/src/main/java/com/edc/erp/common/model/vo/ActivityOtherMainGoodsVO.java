package com.edc.erp.common.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 赠品活动其它主商品表（主要用于买A+B送什么的活动）
 * @author lishaobo
 * @date 2023-1-6
 */
@Data
public class ActivityOtherMainGoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 其它主商品代码 */
    @ApiModelProperty(name = "otherGoodsCode", value = "其它主商品代码")
    private String otherGoodsCode;

    /**
     * 数量
     */
    @ApiModelProperty(name = "goodsNum", value = "数量")
    private BigDecimal goodsNum;

    /** 组合条件(并且，或者) */
    @ApiModelProperty(name = "combination", value = "组合条件(并且，或者)")
    private String combination;

    /** 促销分组主键 */
    @ApiModelProperty(name = "promotionalActivityId", value = "促销分组主键")
    private Integer promotionalActivityId;

    /** 其它主商品名称 */
    @ApiModelProperty(name = "goodsName", value = "其它主商品名称")
    private String goodsName;
}
