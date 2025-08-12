package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 配销分货单和门店商品明细结果集
 * @author lx
 * @since 2022-12-06 16:18:04
 */
@Data
public class OrdDisOrderDistOut extends BackHeaderOrdDistributionOrderOut implements Serializable{

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(name = "orderDistributionDetailOuts",value = "配销分货门店商品结果集")
    private List<OrdDisOrderDistributionDetailOut> orderDistributionDetailOuts;
}
