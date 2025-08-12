package com.edc.erp.wholesale.model.out.returns;

import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 添加商品代码中文(WholesaleReturnDetailAndGoodsStrOut)批发退货单详情出参
 *
 * @author wanglidong
 * @since 2022/11/10 19:49
 */
@Data
@EqualsAndHashCode
public class WholesaleReturnDetailAndGoodsStrOut extends WholesaleReturnDetail implements Serializable {

    /**
     * 商品代码中文
     */
    private String goodsTypeStr;

    @ApiModelProperty(name = "businessQty",value = "业务可用库存")
    private BigDecimal businessQty;

    @ApiModelProperty(name = "checkAmount",value = "审核金额")
    private BigDecimal checkAmount;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;

    @ApiModelProperty(name = "standardSpecs", value = "规格集合")
    private List<StandardSpecOut> standardSpecs;
}
