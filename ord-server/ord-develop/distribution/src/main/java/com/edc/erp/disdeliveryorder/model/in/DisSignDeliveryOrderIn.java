package com.edc.erp.disdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-11-03 15:07
 */
@Data
public class DisSignDeliveryOrderIn extends BaseEntity {

    @ApiModelProperty(name = "deliveryOrderId",required = true, value = "配货单主键")
    @NotNull(message = "请指定配货单")
    private Long deliveryOrderId;

    @ApiModelProperty(name = "orgCode",required = true, value = "组织代码")
    @NotEmpty(message = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "bizOrgCode",required = true, value = "组织代码")
    @NotEmpty(message = "组织代码")
    private String bizOrgCode;

    /**
     * 出货整箱件数
     */
    @ApiModelProperty(name = "shipmentWholePackageQuantity", value = "出货整箱件数")
    private BigDecimal shipmentWholePackageQuantity;

    /**
     * 出货纸箱数
     */
    @ApiModelProperty(name = "shipmentCartonsQuantity", value = "出货纸箱数")
    private BigDecimal shipmentCartonsQuantity;

    /**
     * 出货物流箱数
     */
    @ApiModelProperty(name = "shipmentLogisticsBoxQuantity", value = "出货物流箱数")
    private BigDecimal shipmentLogisticsBoxQuantity;

    /**
     * 整箱件数
     */
    @ApiModelProperty(name = "wholePackageQuantity", required = true,value = "整箱件数")
    @NotNull(message = "请输入整箱件数")
    private BigDecimal wholePackageQuantity;

    /**
     * 纸箱数
     */
    @ApiModelProperty(name = "cartonsQuantity", required = true,value = "纸箱数")
    @NotNull(message = "请输入纸箱数")
    private BigDecimal cartonsQuantity;

    /**
     * 物流箱数
     */
    @ApiModelProperty(name = "logisticsBoxQuantity",required = true, value = "物流箱数")
    @NotNull(message = "请输入物流箱数")
    private BigDecimal logisticsBoxQuantity;

    /**
     * 配货差异备注
     */
    @ApiModelProperty(name = "differencesRemark", value = "配货差异备注")
    private String differencesRemark;

    /**
     * 门店签字
     */
    @ApiModelProperty(name = "signature",required = true, value = "门店签字")
    @NotEmpty(message = "请上传门店签字")
    private String signature;

    /**
     * 附件地址集合
     */
    @ApiModelProperty(name = "attachmentUrlList", value = "附件地址集合")
    private List<String> attachmentUrlList;

    /**
     * 附件类型
     */
    @ApiModelProperty(name = "attachmentType", value = "附件类型，1：签收，2：收货")
    private Integer attachmentType;
}
