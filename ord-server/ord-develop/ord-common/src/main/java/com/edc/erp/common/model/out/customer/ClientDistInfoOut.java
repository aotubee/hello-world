package com.edc.erp.common.model.out.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户配送信息对象
 * @author lx
 * @since 2022-10-18 18:54:02
 */
@Data
public class ClientDistInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 客户名称 */
    @ApiModelProperty(name = "clientName", value = "客户名称")
    private String clientName;

    /** 客户代码 + 名称 */
    @ApiModelProperty(name = "clientCodeStr", value = "客户代码 + 名称")
    private String clientCodeStr;

    /** 价格组代码 */
    @ApiModelProperty(name = "priceGroupCode",value = "价格组代码")
    private String priceGroupCode;

    /** 价格组代码 + 名称 */
    @ApiModelProperty(name = "priceGroupCodeStr", value = "价格组代码 + 名称")
    private String priceGroupCodeStr;

    /** 收货人 */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /** 收货人电话 */
    @ApiModelProperty(name = "consigneePhone", value = "收货人电话")
    private String consigneePhone;

    /** 省编码 */
    @ApiModelProperty(name = "provinceAreaCode", value = "省编码")
    private String provinceAreaCode;

    /** 市编码 */
    @ApiModelProperty(name = "cityAreaCode", value = "市编码")
    private String cityAreaCode;

    /** 区编码 */
    @ApiModelProperty(name = "districtAreaCode", value = "区编码")
    private String districtAreaCode;

    /** 详细地址 */
    @ApiModelProperty(name = "addressDetail", value = "详细地址")
    private String addressDetail;

    /** 省/市/区/ + 详细地址 */
    @ApiModelProperty(name = "shipAddress", value = "收货地址")
    private String shipAddress;

    /** 配送方式 */
    @ApiModelProperty(name = "distributionWay",value = "配送方式")
    private String distributionWay;

    /** 配送方式 - 中文 */
    @ApiModelProperty(name = "distributionWayStr",value = "配送方式 - 中文")
    private String distributionWayStr;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "客户ID")
    private Integer clientId;

    @ApiModelProperty(value = "订单优先级")
    private String orderPriority;
}
