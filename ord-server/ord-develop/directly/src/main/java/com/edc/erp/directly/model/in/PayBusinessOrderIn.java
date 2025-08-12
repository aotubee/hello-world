package com.edc.erp.directly.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author fxw
 * @description: 在线支付入参
 * @since 2022/10/18 19:47
 */
@Data
public class PayBusinessOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "付款方式",example = "小浦支付:0007004,纯余额",required = true)
    @NotEmpty(message = "付款方式不能为空")
    private String onlinePayType;

    @ApiModelProperty(value = "业务类型方式", required = true,example = "订货订单：12002502，充值订单：12002503，直送配货出订单：12002504,新订单流：12002505")
    @NotEmpty(message = "业务类型不能为空")
    private String businessOrderType;

    @ApiModelProperty(value = "支付业务订单id", required = true)
    @NotEmpty(message = "订单不能为空")
    private List<Long> businessOrderIdList;

//    @ApiModelProperty(value = "订货清单基础信息集合")
//    private List<ListInfoOut> purchaseListInfoOutList;

    //private String ipUrl;

    @ApiModelProperty(value = "业务组织代码", required = true)
    @NotEmpty(message = "业务组织代码不能为空")
    private String bizOrgCode;

    @ApiModelProperty(value = "门店代码", required = true)
    @NotEmpty(message = "门店代码不能为空")
    private String storeCode;

    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * private String mobile
     */
    @ApiModelProperty(value = "支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "支付宝微信支付时用户ID")
    private String openUserId;
}
