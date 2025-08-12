package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PresaleActivityListOut implements Serializable {
    private static final long serialVersionUID = 4635132204096852869L;
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
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;
    @ApiModelProperty(name = "statusDesc", value = "状态描述")
    private String statusDesc;
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
     * 商品数
     */
    @ApiModelProperty(name = "totalGoodsQty", value = "商品数")
    private Integer totalGoodsQty;
    @ApiModelProperty(name = "totalStoreQty", value = "门店数")
    private Integer totalStoreQty;
}
