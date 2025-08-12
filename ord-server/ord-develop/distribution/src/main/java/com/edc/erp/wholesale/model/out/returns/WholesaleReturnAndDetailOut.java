package com.edc.erp.wholesale.model.out.returns;

import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * 根据批发退货单订单号查询订单详情(WholesaleReturnAndDetailOut)出参
 *
 * @author wanglidong
 * @since 2022/11/10 19:50
 */
@Data
public class WholesaleReturnAndDetailOut extends WholesaleReturns implements Serializable {

    /**
     * 批发退货单详情
     */
    @ApiModelProperty(name = "wholesaleReturnDetailList", value = "批发退货单详情")
    private List<WholesaleReturnDetailAndGoodsStrOut> wholesaleReturnDetailList;

    /**
     * 客户信息
     */
    @ApiModelProperty(name = "clientMessage", value = "客户信息")
    private String clientMessage;

    /**
     * 客户名称
     */
    @ApiModelProperty(name = "clientName", value = "客户名称")
    private String clientName;

    /**
     * 批发价格组
     */
    @ApiModelProperty(name = "priceGroup", value = "批发价格组")
    private String priceGroup;

    /**
     * 批发价格组名称
     */
    @ApiModelProperty(name = "priceGroupName", value = "批发价格组名称")
    private String priceGroupName;

    /**
     * 退货单状态中文
     */
    @ApiModelProperty(name = "returnStatusStr", value = "退货单状态中文")
    private String returnStatusStr;

    /**
     * 退货原因中文
     */
    @ApiModelProperty(name = "returnsReasonStr",value = "退货原因中文")
    private String returnsReasonStr;
}
