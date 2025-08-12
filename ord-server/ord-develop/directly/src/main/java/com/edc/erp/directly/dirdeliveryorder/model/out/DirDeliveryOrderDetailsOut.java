/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.directly.dirdeliveryorder.model.out;


import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * PpDeliveryOrderDetailsOut返回类
 *
 * @author liwenqiang
 */
@Data
public class DirDeliveryOrderDetailsOut extends OrdDirDeliveryDetail implements Serializable {


    /**
     * 配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;

    /**
     * 配送方式中文
     */
    @ApiModelProperty(name = "distributionTypeValue", value = "配送方式中文")
    private String distributionTypeValue;


    /** 仓位名称*/
    @ApiModelProperty(name = "positionName", value = "仓位名称")
    private String positionName;

    /** 仓位*/
    @ApiModelProperty(name = "position", value = "仓位")
    private String position;
    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类")
    private String sortName;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 订单方名称 */
    @ApiModelProperty(name = "vendorName", value = "订单方名称")
    private String vendorName;

    /** 商品属性*/
    @ApiModelProperty(name = "goodsTypeStr", value = "商品属性")
    private String goodsTypeStr;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;

    @ApiModelProperty(value = "导入序号")
    private Integer importIndex;
}
