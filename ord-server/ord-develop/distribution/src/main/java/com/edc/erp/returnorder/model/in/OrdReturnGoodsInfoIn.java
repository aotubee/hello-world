package com.edc.erp.returnorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货商品入参
 *
 * @author yaojinpeng
 * @since 2022/10/24 15:47
 */
@Data
public class OrdReturnGoodsInfoIn extends BaseEntity {

    /**
     * 退货单主键
     */
    @ApiModelProperty(name = "returnOrderDetailId", value = "退货单主键")
    private Integer returnOrderId;

    /**
     * 退货单主键
     */
    @ApiModelProperty(name = "returnOrderDetailId", value = "退货单详细主键")
    private Integer returnOrderDetailId;

    /**
     * 行号
     */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;
    /**
     * 商品sku
     */
    @ApiModelProperty(name = "goodsCode", value = "商品code")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsCode", value = "商品名称")
    private String goodsName;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "orgGoodsId",value = "组织商品id")
    private Integer orgGoodsId;

    @ApiModelProperty(name = "distributionSpecification",value = "配货规格")
    private String distributionSpecification;

    @ApiModelProperty(name = "distributionSpecificationUnit",value = "单位")
    private String distributionSpecificationUnit;

    @ApiModelProperty(name = "distributionSpecification",value = "配货规格的数量")
    private BigDecimal distributionSpecificationNum;

    @ApiModelProperty(name = "goodsType",value = "品类属性")
    private String goodsType;
    @ApiModelProperty(name = "remark",value = "备注")
    private String  remark;

    /**
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请包装数量
     */
    @ApiModelProperty(name = "applyPackageQuantity", value = "申请退货包装数量")
    private BigDecimal applyPackageQuantity;

    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private  BigDecimal applyReturnAmount;
    @ApiModelProperty(name = "auditReturnQuantity",value = "审核退货数量")
    private  BigDecimal auditReturnQuantity;

    @ApiModelProperty(name = "auditPackageQuantity",value = "审核退货包装数量")
    private  BigDecimal auditPackageQuantity;

    @ApiModelProperty(name = "auditReturnAmount", value = "审核退货金额")
    private  BigDecimal auditReturnAmount;

    @ApiModelProperty(name = "actualReturnQuantity",value = "实际退货数量")
    private BigDecimal actualReturnQuantity;

    @ApiModelProperty(name = "actualReturnQuantity",value = "实际退货包装数量")
    private BigDecimal actualPackageQuantity;

    @ApiModelProperty(name = "actualReturnAmount", value = "实际退货包金额")
    private  BigDecimal actualReturnAmount;

    /**
     * 退货原因
     */
    @ApiModelProperty(name = "returnReason", value = "退货原因", required = true)
    private String returnReason;

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
     * 退货单价
     */
    @ApiModelProperty(name = "returnUnitPrice", value = "退货单价")
    private BigDecimal returnUnitPrice;

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
     * 配销价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配销价")
    private BigDecimal distributionPrice;

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


}
