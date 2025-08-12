package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PresaleActivityDetailOut implements Serializable {
    @ApiModelProperty(name = "id", required = true, value = "预售活动id")
    private Long id;

    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    @ApiModelProperty(name = "statusDesc", value = "状态描述")
    private String statusDesc;

    @ApiModelProperty(name = "beginSaleDate", required = true, value = "预售活动开始时间")
    private LocalDateTime beginSaleDate;

    @ApiModelProperty(name = "endSaleDate", required = true, value = "预售活动结束时间")
    private LocalDateTime endSaleDate;

    @ApiModelProperty(name = "beginOrderDate", required = true, value = "可订货开始时间")
    private LocalDateTime beginOrderDate;

    @ApiModelProperty(name = "endOrderDate", required = true, value = "可订货结束时间")
    private LocalDateTime endOrderDate;

    @ApiModelProperty(name = "profile", value = "活动简介")
    private String profile;

    @ApiModelProperty(name = "presaleImage", required = true, value = "app图片")
    private String presaleImage;

    @ApiModelProperty(name = "isLimitBuy", required = true, value = "是否活动限购")
    private Integer isLimitBuy;

    @ApiModelProperty(name = "limitBuyTime", value = "限购次数")
    private Integer limitBuyTime;

    @ApiModelProperty(name = "promotionalPrice", required = true, value = "促销价")
    private BigDecimal promotionalPrice;

    @ApiModelProperty(name = "activityGoodsList", required = true, value = "活动商品")
    private List<OrdDisPresaleActivityGoods> activityGoodsList;

    @ApiModelProperty(name = "activityStoreList", required = true, value = "活动门店")
    private List<OrdDisPresaleActivityStore> activityStoreList;

    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
