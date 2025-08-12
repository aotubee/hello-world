package com.edc.erp.returnorder.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 退货单详情表(DisReturnDetail)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-21 18:26:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_return_detail")
@ApiModel(value = "DisReturnDetail", description = "退货单详情表")
public class OrdDisReturnDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 退货单主键
     */
    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Integer returnOrderId;

    /**
     * 行号
     */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /**
     * 组织商品id
     */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 配货规格数量（1*12中的12）
     */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量（1*12中的12）")
    private BigDecimal distributionSpecificationNum;

    /**
     * 商品配货规格（1*12）
     */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /**
     * 商品配货规格单位（件/箱）
     */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /**
     * 商品主图
     */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /**
     * 退货原因
     */
    @ApiModelProperty(name = "returnReason", value = "退货原因")
    private String returnReason;

    /**
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货包装数
     */
    @ApiModelProperty(name = "applyPackageQuantity", value = "申请退货包装数")
    private BigDecimal applyPackageQuantity;

    /**
     * 申请退货金额
     */
    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /**
     * 审核退货数量
     */
    @ApiModelProperty(name = "auditReturnQuantity", value = "审核退货数量")
    private BigDecimal auditReturnQuantity;

    /**
     * 审核退货包装数
     */
    @ApiModelProperty(name = "auditPackageQuantity", value = "审核退货包装数")
    private BigDecimal auditPackageQuantity;

    /**
     * 审核退货金额
     */
    @ApiModelProperty(name = "auditReturnAmount", value = "审核退货金额")
    private BigDecimal auditReturnAmount;

    /**
     * 实际退货数量
     */
    @ApiModelProperty(name = "actualReturnQuantity", value = "实际退货数量")
    private BigDecimal actualReturnQuantity;

    /**
     * 实际退货包装数
     */
    @ApiModelProperty(name = "actualPackageQuantity", value = "实际退货包装数")
    private BigDecimal actualPackageQuantity;

    /**
     * 实际退货金额
     */
    @ApiModelProperty(name = "actualReturnAmount", value = "实际退货金额")
    private BigDecimal actualReturnAmount;

    /**
     * 赠品是否可退
     */
    @ApiModelProperty(name = "isGiftReturn", value = "赠品是否可退")
    private Integer isGiftReturn;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /**
     * 品类属性
     */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /**
     * 退货单价
     */
    @ApiModelProperty(name = "returnUnitPrice", value = "退货单价")
    private BigDecimal returnUnitPrice;

    /**
     * 退货去税金额
     */
    @ApiModelProperty(name = "returnExceptTaxAmount", value = "退货去税金额")
    private BigDecimal returnExceptTaxAmount;

    /**
     * 退货税额
     */
    @ApiModelProperty(name = "returnTaxAmount", value = "退货税额")
    private BigDecimal returnTaxAmount;

    /**
     * 仓储库存价
     */
    @ApiModelProperty(name = "wrhPrice", value = "仓储库存价")
    private BigDecimal wrhPrice;

    /**
     * 仓储成本金额
     */
    @ApiModelProperty(name = "wrhCostAmount", value = "仓储成本金额")
    private BigDecimal wrhCostAmount;

    /**
     * 仓储成本去税金额
     */
    @ApiModelProperty(name = "wrhExceptTaxAmount", value = "仓储成本去税金额")
    private BigDecimal wrhExceptTaxAmount;

    /**
     * 仓储成本税额
     */
    @ApiModelProperty(name = "wrhTaxAmount", value = "仓储成本税额")
    private BigDecimal wrhTaxAmount;

    /**
     * 门店库存价
     */
    @ApiModelProperty(name = "storeStockPrice", value = "门店库存价")
    private BigDecimal storeStockPrice;

    /**
     * 门店成本金额
     */
    @ApiModelProperty(name = "storeCostAmount", value = "门店成本金额")
    private BigDecimal storeCostAmount;

    /**
     * 门店成本去税金额
     */
    @ApiModelProperty(name = "storeExceptTaxAmount", value = "门店成本去税金额")
    private BigDecimal storeExceptTaxAmount;

    /**
     * 门店成本税额
     */
    @ApiModelProperty(name = "storeTaxAmount", value = "门店成本税额")
    private BigDecimal storeTaxAmount;

    /**
     * 税率
     */
    @ApiModelProperty(name = "sellTax", value = "税率")
    private BigDecimal sellTax;

    /**
     * 配送价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配送价")
    private BigDecimal distributionPrice;

    @ApiModelProperty(name = "otherGoodsCode", value = "映射商品")
    private String otherGoodsCode;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(value = "效期码")
    private String expiry;
}
