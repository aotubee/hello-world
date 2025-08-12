package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 配销分货门店商品 查询入参
 * @author lx
 * @since 2022-11-14 17:05:36
 */
@Data
public class QueryOrderDistributionDetailIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "distributionOrderNo", value = "配销分货单号")
    private String distributionOrderNo;

    @ApiModelProperty(name = "distributionOrderId", value = "分货单id")
    private Long distributionOrderId;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织code")
    private String bizOrgCode;
}
