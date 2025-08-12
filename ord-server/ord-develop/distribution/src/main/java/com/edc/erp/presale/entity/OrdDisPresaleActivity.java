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
@Table(name = "ord_dis_presale_activity")
@ApiModel(value = "OrdDisPresaleActivity", description = "预售活动")
public class OrdDisPresaleActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /**
     * 商品数
     */
    @ApiModelProperty(name = "totalGoodsQty", value = "商品数")
    private Integer totalGoodsQty;

    /**
     * 门店数
     */
    @ApiModelProperty(name = "totalStoreQty", value = "门店数")
    private Integer totalStoreQty;

    /**
     * 预售活动开始时间
     */
    @ApiModelProperty(name = "beginSaleDate", value = "预售活动开始时间")
    private LocalDateTime beginSaleDate;

    /**
     * 预售活动结束时间
     */
    @ApiModelProperty(name = "endSaleDate", value = "预售活动结束时间")
    private LocalDateTime endSaleDate;

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
     * 活动简介
     */
    @ApiModelProperty(name = "profile", value = "活动简介")
    private String profile;

    /**
     * app图片
     */
    @ApiModelProperty(name = "presaleImage", value = "app图片")
    private String presaleImage;

    /**
     * 是否活动限购
     */
    @ApiModelProperty(name = "isLimitBuy", value = "是否活动限购")
    private Integer isLimitBuy;

    /**
     * 限购次数
     */
    @ApiModelProperty(name = "limitBuyTime", value = "限购次数")
    private Integer limitBuyTime;

    /**
     * 促销价
     */
    @ApiModelProperty(name = "promotionalPrice", value = "促销价")
    private BigDecimal promotionalPrice;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

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
