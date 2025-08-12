package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 商品配送规格返回实体类
 * @since 2022/10/19 15:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgGoodsSpecificationOut implements Serializable {
    private static final long serialVersionUID = -7950161287851188727L;

    /**
     * 单位
     */
    @ApiModelProperty(name = "unit", value = "单位")
    private String unit;

    /**
     * 规格说明
     */
    @ApiModelProperty(name = "specification", value = "规格说明")
    private String specification;

    /**
     * 规格
     */
    @ApiModelProperty(name = "specificationNum", value = "规格")
    private BigDecimal specificationNum;
}
