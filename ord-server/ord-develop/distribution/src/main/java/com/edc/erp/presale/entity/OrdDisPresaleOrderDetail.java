package com.edc.erp.presale.entity;


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
 * 退货单(DisReturn)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_presale_order_detail")
@ApiModel(value = "OrdDisPresaleOrderDetail", description = "预售订单明细")
public class OrdDisPresaleOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "presaleOrderId", value = "预售订单id")
    private Long presaleOrderId;

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
     * 商品图片
     */
    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 预售活ID
     */
    @ApiModelProperty(name = "presaleActivityId", value = "预售活动ID")
    private Long presaleActivityId;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动单号")
    private String presaleActivityNo;

    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /**
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /**
     * 配销价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配销价")
    private BigDecimal distributionPrice;

    /**
     * 配销金额
     */
    @ApiModelProperty(name = "distributionAmount", value = "配销金额")
    private BigDecimal distributionAmount;

    @ApiModelProperty(name = "payAmount", value = "应付金额")
    private BigDecimal payAmount;

    /**
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "包装数")
    private BigDecimal packageQuantity;

    @ApiModelProperty(name = "goodsQuantity", value = "订货数量")
    private BigDecimal goodsQuantity;

    /**
     * 是否赠品
     */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

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
