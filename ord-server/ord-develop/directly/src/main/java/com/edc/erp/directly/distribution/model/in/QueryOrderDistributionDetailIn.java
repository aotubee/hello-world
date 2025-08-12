package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 直营分货门店商品 查询入参
 * @author lx
 * @since 2022-11-14 17:05:36
 */
@Data
public class QueryOrderDistributionDetailIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 直营配货分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "直营分货单号")
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
