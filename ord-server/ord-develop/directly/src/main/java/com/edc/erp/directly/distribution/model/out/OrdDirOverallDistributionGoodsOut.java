package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrdDisOverallDistributionGoodsOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/19 11:11
 **/
@Data
public class OrdDirOverallDistributionGoodsOut implements Serializable {
    private static final long serialVersionUID = -7321829551521446533L;

    @ApiModelProperty(name = "detailId", value = "明细id")
    private Long detailId;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "distributionQuantity", value = "分货数量")
    private BigDecimal distributionQuantity;

    /**
     * 配货规格
     */
    @ApiModelProperty(name = "distributionSpecification", value = "配货规格")
    private String distributionSpecification;
}
