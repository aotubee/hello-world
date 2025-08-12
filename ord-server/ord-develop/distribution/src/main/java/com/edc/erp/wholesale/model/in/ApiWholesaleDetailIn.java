package com.edc.erp.wholesale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName ApiWholesaleDetailIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/10 10:20
 **/
@Data
public class ApiWholesaleDetailIn implements Serializable {
    private static final long serialVersionUID = -7496560298173913380L;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量")
    private Integer applyQuantity;

    /** 单价 */
    @ApiModelProperty(name = "unitPrice", value = "单价")
    private BigDecimal unitPrice;

//    /** 审请金额 */
//    @ApiModelProperty(name = "applyAmount", value = "审请金额")
//    private BigDecimal applyAmount;


    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则")
    private String returnPrinciple;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

}
