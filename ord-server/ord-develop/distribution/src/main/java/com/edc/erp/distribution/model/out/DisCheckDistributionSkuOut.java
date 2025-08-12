package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;

@Data
public class DisCheckDistributionSkuOut implements Serializable {

    @ApiModelProperty(name = "legalOrderCartGoodsOutList", value = "合法购物车商品数据集合")
    private List<OrderCartOut> legalOrderCartGoodsOutList;

    @ApiModelProperty(name = "errorMessageSet", value = "商品异常信息")
    private LinkedHashSet<String> errorMessageSet;
}
