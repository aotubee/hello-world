package com.edc.erp.common.model.out.purchase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @return: 中转通知采购实体类
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
@ApiModel(value = "TransferShipmentPushPurchaseBackVO",description = "中转批发出货单通知采购实体类")
public class TransferShipmentPushPurchaseBackVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "vendorCode",name = "订单方代码")
    private String vendorCode;

    @ApiModelProperty(name = "purBatchNumber", value = "推采购批次号")
    private String purBatchNumber;

    /**
     * 商品信息集合
     */
    @ApiModelProperty(value = "goodsDtls",name = "商品信息集合")
    @Valid
    private List<GoodsDtlsVO> goodsDtls;

    /**
     * 采购订单号
     */
    @ApiModelProperty(value = "purchaseOrderNo",name = "采购订单号")
    @NotEmpty(message = "采购订单号不能为空")
    private String purchaseOrderNo;

    @ApiModelProperty(value = "bizOrgCode",name = "bizOrgCode")
    private String bizOrgCode;

    @ApiModelProperty(value = "pushPurProgress",name = "采购推送进度")
    private Integer pushPurProgress;
}
