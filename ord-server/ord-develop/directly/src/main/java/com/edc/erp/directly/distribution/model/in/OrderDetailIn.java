package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: 列表查询入参
 * @since 2022/11/15 11:19
 */
@Data
public class OrderDetailIn extends Page implements Serializable {
    private static final long serialVersionUID = 4255032959829553119L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /** 直营配货订单主键 */
    @ApiModelProperty(name = "orderId", value = "直营配货订单主键")
    private Long orderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 商品条形码 */
    @ApiModelProperty(name = "barCode", value = "商品条形码")
    private String barCode;

    /** 商品小分类 */
    @ApiModelProperty(name = "smallSort", value = "商品小分类")
    private String smallSort;

    /** 商品标签 */
    @ApiModelProperty(name = "tagName", value = "商品标签")
    private String tagName;

    /** 规格单位 */
    @ApiModelProperty(name = "specificationUnit", value = "规格单位")
    private String specificationUnit;

    /** 商品配货规格（1*1,1*2） */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*1,1*2）")
    private String distributionSpecification;

    /** 配货规格数量 */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量")
    private BigDecimal distributionSpecificationNum;

    /** 商品配货规格单位（件/箱） */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /** 仓位代码 */
    @ApiModelProperty(name = "position", value = "仓位代码")
    private String position;

    /** 配送方式 */
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;

    /** 订货数量 */
    @ApiModelProperty(name = "quantity", value = "订货数量")
    private BigDecimal quantity;

    /** 订货包装数量 */
    @ApiModelProperty(name = "packageQuantity", value = "订货包装数量")
    private BigDecimal packageQuantity;

    /** 支付单价 */
    @ApiModelProperty(name = "orderUnitPrice", value = "支付单价")
    private BigDecimal orderUnitPrice;

    /** 支付金额 */
    @ApiModelProperty(name = "orderAmount", value = "支付金额")
    private BigDecimal orderAmount;

    /** 直营配货价（原价） */
    @ApiModelProperty(name = "originalPrice", value = "直营配货价（原价）")
    private BigDecimal originalPrice;

    /** 直营配货金额 */
    @ApiModelProperty(name = "originaAmount", value = "直营配货金额")
    private BigDecimal originaAmount;

    /** 活动单价 */
    @ApiModelProperty(name = "activityUnitPrice", value = "活动单价")
    private BigDecimal activityUnitPrice;

    /** 活动单号 */
    @ApiModelProperty(name = "activityNo", value = "活动单号")
    private String activityNo;

    /** 商品零售价 */
    @ApiModelProperty(name = "suggestedRetailPrice", value = "商品零售价")
    private BigDecimal suggestedRetailPrice;

    /** 是否参加活动 */
    @ApiModelProperty(name = "isActivity", value = "是否参加活动")
    private Integer isActivity;

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

    /** 是否可退 */
    @ApiModelProperty(name = "allowDistributionReturn", value = "是否可退")
    private Integer allowDistributionReturn;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /** 实付单价(直营配货) */
    @ApiModelProperty(name = "realUnitPrice", value = "实付单价(直营配货)")
    private BigDecimal realUnitPrice;

    /** 实付金额(直营配货) */
    @ApiModelProperty(name = "realAmount", value = "实付金额(直营配货)")
    private BigDecimal realAmount;
}
