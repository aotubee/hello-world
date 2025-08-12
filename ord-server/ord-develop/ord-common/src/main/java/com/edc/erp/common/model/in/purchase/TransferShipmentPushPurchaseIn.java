package com.edc.erp.common.model.in.purchase;

import com.edc.erp.common.model.in.purchase.GoodsDtlsIn;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @return: 采购通知采购实体类
 * @Author: ZY
 * @Date: 2022/11/23
 */
@Data
@ApiModel(value = "TransferShipmentPushPurchaseIn",description = "中转批发出单通知采购实体类")
public class TransferShipmentPushPurchaseIn implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "vendorCode",name = "订单方代码")
    private String vendorCode;

    @ApiModelProperty(name = "purBatchNumber", value = "推采购批次号")
    private String purBatchNumber;

    @ApiModelProperty(value = "goodsDtls",name = "商品信息集合")
    private List<GoodsDtlsIn> goodsDtls;

    @ApiModelProperty(value = "bizOrgCode",name = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "stockCode",name = "仓位代码")
    private String stockCode;

    @ApiModelProperty(value = "warehouseCode",name = "仓储代码")
    private String warehouseCode;

    @ApiModelProperty(value = "订单优先级")
    private String orderPriority;

}
