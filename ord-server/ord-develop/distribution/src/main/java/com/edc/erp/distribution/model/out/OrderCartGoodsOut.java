package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车商品（新）
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-06-28 15:31
 */
@Data
public class OrderCartGoodsOut extends BaseOrderCartGoodsOut {

    @ApiModelProperty(name = "giftOutList", value = "赠送商品集合")
    private List<OrderCartGoodsOut> giftOutList;

    @ApiModelProperty(name = "activityNote", value = "活动描述")
    private String activityNote;

    @ApiModelProperty(name = "activityType", value = "活动类型")
    private String activityType;

    @ApiModelProperty(name = "activityCode", value = "活动编号")
    private String activityCode;

    @ApiModelProperty(name = "activityName", value = "活动名称")
    private String activityName;

    @ApiModelProperty(name = "existsQuantity", value = "已存在数量")
    private BigDecimal existsQuantity;

    @ApiModelProperty(name = "isEnable",value = "是否启用")
    private Integer isEnable;

    @ApiModelProperty(name = "isNeedWarningTag", value = "是否需要预警打标")
    private Integer isNeedWarningTag;

    @ApiModelProperty(name = "positionStr", value = "仓位中文")
    private String positionStr;

    @ApiModelProperty(name = "optionalGiftCodeList", value = "选择赠送商品代码集合")
    private List<String> optionalGiftCodeList;

    @ApiModelProperty(name = "canQty", value = "可订数量")
    private BigDecimal canQty;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;
}
