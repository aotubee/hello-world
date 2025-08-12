package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName PresaleActiviryInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 11:34
 **/
@Data
public class PresaleActivityInfoForAppOut implements Serializable {
    private static final long serialVersionUID = -2195758385803365732L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

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

    @ApiModelProperty(name = "presaleActivityGoodsInfoOutList", value = "预售活动商品")
    private List<PresaleActivityGoodsForAppOut> presaleActivityGoodsInfoOutList;
}
