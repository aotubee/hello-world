package com.edc.erp.directly.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrdDirDistributionDetailAuditIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/4 15:21
 **/
@Data
public class OrdDirDistributionDetailAuditIn implements Serializable {
    private static final long serialVersionUID = 2022377836046145555L;

    @ApiModelProperty(name = "detailId", value = "明细id")
    private Long detailId;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "distributionQuantity", value = "直营分货数量")
    private BigDecimal distributionQuantity;
}
