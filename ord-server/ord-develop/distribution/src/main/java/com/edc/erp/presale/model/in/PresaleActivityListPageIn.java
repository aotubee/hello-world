package com.edc.erp.presale.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PresaleActivityListPageIn extends Page implements Serializable {
    private static final long serialVersionUID = -4906589505084973240L;
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;
    /**
     * 活动状态
     */
    @ApiModelProperty(name = "status", value = "活动状态")
    private String status;
    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;
    /**
     * 活动单号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
    private String presaleActivityNo;
    /**
     * 活动开始日期
     */
    @ApiModelProperty(name = "beginSaleDate", value = "活动开始日期")
    private String beginSaleDate;
    /**
     * 活动结束日期
     */
    @ApiModelProperty(name = "endSaleDate", value = "活动结束日期")
    private String endSaleDate;
    /**
     * 可订货开始日期
     */
    @ApiModelProperty(name = "beginOrderDate", value = "可订货开始日期")
    private String beginOrderDate;
    /**
     * 可订货结束日期
     */
    @ApiModelProperty(name = "endOrderDate", value = "可订货结束日期")
    private String endOrderDate;
    /**
     * 组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;
}
