package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName SubmitPresaleOrderActivityIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 15:18
 **/
@Data
@ApiModel(value = "SubmitPresaleOrderActivityIn", description = "提交预售订单活动入参")
public class SubmitPresaleOrderActivityIn implements Serializable {
    private static final long serialVersionUID = -3944427270770633923L;

    @ApiModelProperty(name = "presaleActivityId", value = "预售活动ID")
    private Long presaleActivityId;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityQty", value = "购买数量")
    private BigDecimal presaleActivityQty;

    /**
     * 预售商品明细集合
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售商品明细集合")
    private List<SubmitPresaleOrderGoodsIn> presaleOrderDetailInList;

}
