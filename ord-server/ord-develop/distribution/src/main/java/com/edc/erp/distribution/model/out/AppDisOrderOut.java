package com.edc.erp.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-23 11:45
 */
@Data
public class AppDisOrderOut extends BaseEntity {

    @ApiModelProperty(name = "orderId", value = "订货id")
    private Long orderId;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "orderStatusCodeStr", value = "订货单状态中文")
    private String orderStatusCodeStr;

    @ApiModelProperty(name = "orderAmount", value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "payableAmount", value = "应付金额")
    private BigDecimal payableAmount;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;

    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;

    @ApiModelProperty(name = "sourceCodeStr", value = "订货类型中文")
    private String sourceCodeStr;

    @ApiModelProperty(name = "isCanEdit", value = "是否能编辑")
    private Integer isCanEdit;

    @ApiModelProperty(name = "isCanInvalid", value = "是否能作废")
    private Integer isCanInvalid;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(name = "appTopOrderGoodsOutList", value = "top3商品图片集合")
    private List<AppTopDisOrderGoodsOut> appTopOrderGoodsOutList;

    @ApiModelProperty(name = "payTimeCountdown", value = "支付倒计时(秒)")
    private Long payTimeCountdown;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "orderIdentification", value = "门店标识")
    private String orderIdentification;

    @ApiModelProperty(name = "orderIdentificationStr", value = "门店标识")
    private String orderIdentificationStr;

}
