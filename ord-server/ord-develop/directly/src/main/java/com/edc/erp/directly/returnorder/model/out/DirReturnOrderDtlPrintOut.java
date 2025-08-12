package com.edc.erp.directly.returnorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description 退货单明细打印出参
 *
 * @author gusiyuan
 * @since 2023/10/9 18:53
 */
@Data
public class DirReturnOrderDtlPrintOut implements Serializable {

    private static final long serialVersionUID = 5453079524767688209L;

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

    /** 退货单价 */
    @ApiModelProperty(name = "returnUnitPrice", value = "退货单价")
    private BigDecimal returnUnitPrice;
}
