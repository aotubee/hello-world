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
@Table(name = "ord_dis_presale_assets_detail")
@ApiModel(value = "OrdDisPresaleAssetsDetail", description = "门店资产明细表")
public class OrdDisPresaleAssetsDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(name = "assetsId", value = "资产id")
    private Long assetsId;

    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "商品代码")
    private String status;

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

    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;

    /**
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /**
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;

    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "已订包装数")
    private BigDecimal packageQuantity;

    /**
     * 已订货量
     */
    @ApiModelProperty(name = "orderQuantity", value = "已订货量")
    private BigDecimal orderQuantity;

    /**
     * 剩余订货量
     */
    @ApiModelProperty(name = "surplusQuantity", value = "剩余订货量")
    private BigDecimal surplusQuantity;

    @ApiModelProperty(name = "surplusPackageQuantity", value = "剩余包装数量")
    private String surplusPackageQuantity;

    @ApiModelProperty(name = "presaleActivityId", value = "活动ID")
    private Long presaleActivityId;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
    private String presaleActivityNo;

    /**
     * 可订货开始时间
     */
    @ApiModelProperty(name = "beginOrderDate", value = "可订货开始时间")
    private LocalDateTime beginOrderDate;

    /**
     * 可订货结束时间
     */
    @ApiModelProperty(name = "endOrderDate", value = "可订货结束时间")
    private LocalDateTime endOrderDate;

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
