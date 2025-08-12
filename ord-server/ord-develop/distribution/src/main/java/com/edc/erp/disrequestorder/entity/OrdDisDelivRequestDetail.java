package com.edc.erp.disrequestorder.entity;


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
 * 集货单明细表(OrdDisDelivRequestDetail)实体类
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_deliv_request_detail")
@ApiModel(value = "OrdDisDelivRequestDetail", description = "集货单明细表")
public class OrdDisDelivRequestDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 集货单主键 */
    @ApiModelProperty(name = "requestOrderId", value = "集货单主键")
    private Long requestOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /** 商品主图 */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /** 集货包装数量 */
    @ApiModelProperty(name = "packageQuantity", value = "集货包装数量")
    private BigDecimal packageQuantity;

    /** 集货数量 */
    @ApiModelProperty(name = "quantity", value = "集货数量")
    private BigDecimal quantity;

    /** 集货单价 */
    @ApiModelProperty(name = "requestUnitPrice", value = "集货单价")
    private BigDecimal requestUnitPrice;

    /** 集货金额 */
    @ApiModelProperty(name = "requestOrderAmount", value = "集货金额")
    private BigDecimal requestOrderAmount;

    /** 商品配销价 */
    @ApiModelProperty(name = "originalUnitPrice", value = "商品配销价")
    private BigDecimal originalUnitPrice;

    /** 规格单位（瓶/包） */
    @ApiModelProperty(name = "specificationUnit", value = "规格单位（瓶/包）")
    private String specificationUnit;

    /** 商品配货规格（1*12） */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /** 商品配货规格单位（件/箱） */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /** 配货规格数量（1*12中的12） */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量（1*12中的12）")
    private BigDecimal distributionSpecificationNum;

    /** 小分类 */
    @ApiModelProperty(name = "smallSort", value = "小分类")
    private String smallSort;

    /** 仓位代码 */
    @ApiModelProperty(name = "position", value = "仓位代码")
    private String stockCode;

    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

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

    /**
     * 活动单号
     */
    @ApiModelProperty(name = "activityNo", value = "活动单号")
    private String activityNo;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 仓储代码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /**
     * 赠品基础商品代码
     */
    @ApiModelProperty(name = "baseGoodsCode", value = "赠品基础商品代码")
    private String baseGoodsCode;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;
}
