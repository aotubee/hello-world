package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配销分货门店商品 结果集
 * @author lx
 * @since 2022-11-14 17:05:57
 */
@Data
public class OrdDisOrderDistributionDetailOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配销分货关联表主键
     */
    @ApiModelProperty(name = "id", value = "配销分货关联表主键")
    private Long id;

    /**
     * 配销分货单主键
     */
    @ApiModelProperty(name = "distributionOrderId", value = "配销分货单主键")
    private Long distributionOrderId;

    /**
     * 配销分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "配销分货单号")
    private String distributionOrderNo;

    /**
     * 配销分货数量
     */
    @ApiModelProperty(name = "distributionQuantity", value = "配销分货数量")
    private BigDecimal distributionQuantity;

    /**
     * 配销分货包装数
     */
    @ApiModelProperty(name = "packingNumber", value = "配销分货包装数")
    private BigDecimal packingNumber;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 配销分货金额
     */
    @ApiModelProperty(name = "distributionAmount", value = "配销分货金额")
    private BigDecimal distributionAmount;

    /**
     * 配销价
     */
    @ApiModelProperty(name = "originalPrice", value = "配销价")
    private BigDecimal originalPrice;

    /**
     * 配货规格
     */
    @ApiModelProperty(name = "distributionSpecification", value = "配货规格")
    private String distributionSpecification;

    /**
     * 当前库存
     */
    @ApiModelProperty(name = "wrhInvQty", value = "当前库存")
    private BigDecimal wrhInvQty;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;
}
