package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 直营分货单和门店商品明细结果集
 * @author lx
 * @since 2022-12-06 16:18:04
 */
@Data
public class OrdDirOrderDistOut extends BackHeaderOrdDistributionOrderOut implements Serializable{

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(name = "orderDistributionDetailOuts",value = "只一个分货门店商品结果集")
    private List<OrdDirOrderDistributionDetailOut> orderDistributionDetailOuts;
}
