package com.edc.erp.directly.distribution.entity;

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
 * 订货单详细表(OrdDirOrderDetail)实体类
 *
 * @author wanglidong
 * @since 2022-11-15 17:34:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_detail")
@ApiModel(value = "OrdDirOrderDetail", description = "订货单详细表")
public class OrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */     
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单主键
     */     
    @ApiModelProperty(name = "orderId", value = "订单主键")
    private Long orderId;
    
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
     * 商品条码
     */     
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;
    
    /**
     * 商品标签
     */     
    @ApiModelProperty(name = "tagName", value = "商品标签")
    private String tagName;
    
    /**
     * 商品主图
     */     
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;
    
    /**
     * 规格单位
     */     
    @ApiModelProperty(name = "specificationUnit", value = "规格单位")
    private String specificationUnit;
    
    /**
     * 商品配货规格（1*1,1*2）
     */     
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*1,1*2）")
    private String distributionSpecification;
    
    /**
     * 配货规格数量
     */     
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量")
    private BigDecimal distributionSpecificationNum;
    
    /**
     * 商品配货规格单位（件/箱）
     */     
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;
    
    /**
     * 商品小分类
     */     
    @ApiModelProperty(name = "smallSort", value = "商品小分类")
    private String smallSort;
    
    /**
     * 仓位代码
     */     
    @ApiModelProperty(name = "position", value = "仓位代码")
    private String position;
    
    /**
     * 配送方式
     */     
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;
    
    /**
     * 订货数量
     */     
    @ApiModelProperty(name = "quantity", value = "订货数量")
    private BigDecimal quantity;
    
    /**
     * 订货包装数量
     */     
    @ApiModelProperty(name = "packageQuantity", value = "订货包装数量")
    private BigDecimal packageQuantity;
    
    /**
     * 订货单价
     */     
    @ApiModelProperty(name = "orderUnitPrice", value = "订货单价")
    private BigDecimal orderUnitPrice;
    
    /**
     * 订货金额
     */     
    @ApiModelProperty(name = "orderAmount", value = "订货金额")
    private BigDecimal orderAmount;
    
    /**
     * 配送价（原价）
     */     
    @ApiModelProperty(name = "originalPrice", value = "配送价（原价）")
    private BigDecimal originalPrice;
    
    /**
     * 配送金额
     */     
    @ApiModelProperty(name = "originaAmount", value = "配送金额")
    private BigDecimal originaAmount;
    
    /**
     * 活动类型
     */     
    @ApiModelProperty(name = "activityType", value = "活动类型")
    private String activityType;
    
    /**
     * 活动单价
     */     
    @ApiModelProperty(name = "activityUnitPrice", value = "活动单价")
    private BigDecimal activityUnitPrice;
    
    /**
     * 活动单号
     */     
    @ApiModelProperty(name = "activityNo", value = "活动单号")
    private String activityNo;
    
    /**
     * 商品零售价
     */     
    @ApiModelProperty(name = "suggestedRetailPrice", value = "商品零售价")
    private BigDecimal suggestedRetailPrice;
    
    /**
     * 是否参加活动
     */     
    @ApiModelProperty(name = "isActivity", value = "是否参加活动")
    private Integer isActivity;
    
    /**
     * 是否赠品
     */     
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;
    
    /**
     * 是否可退
     */     
    @ApiModelProperty(name = "allowDistributionReturn", value = "是否可退")
    private Integer allowDistributionReturn;
    
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
     * 是否删除
     */     
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;
    
    /**
     * 品类属性
     */     
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;
    
    /**
     * 整单优惠金额
     */     
    @ApiModelProperty(name = "discountAmount", value = "整单优惠金额")
    private BigDecimal discountAmount;

    /** 实付单价(直营) */
    @ApiModelProperty(name = "realUnitPrice", value = "实付单价(直营)")
    private BigDecimal realUnitPrice;

    /** 实付金额(直营) */
    @ApiModelProperty(name = "realAmount", value = "实付金额(直营)")
    private BigDecimal realAmount;

    /**
     * 赠品基础商品代码
     */
    @ApiModelProperty(name = "baseGoodsCode", value = "赠品基础商品代码")
    private String baseGoodsCode;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;
}