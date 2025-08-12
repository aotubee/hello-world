package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrdDirDistributionImportResultOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/31 14:05
 **/
@Data
public class OrdDisDistributionImportGoodsOut implements Serializable {
    private static final long serialVersionUID = -7621584992656222239L;

    @ApiModelProperty(value = "detailId",name = "明细id")
    private Long detailId;

    @ApiModelProperty(value = "goodsCode",name = "商品代码")
    private String goodsCode;

//    private String errorMessage;
@ApiModelProperty(value = "distributionQuantity",name = "分货数量")
    private BigDecimal distributionQuantity;
}
