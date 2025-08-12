package com.edc.erp.disdeliveryorder.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName WholesaleShipmentDetailIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/1 15:26
 **/
@Data
public class ZKWholesaleReturnDetailIn implements Serializable {
    private static final long serialVersionUID = -5578297477518460551L;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码", required = true)
    @NotBlank(message = "商品代码不能为空!")
    private String goodsCode;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量", required = true)
    @NotNull(message = "申请数量不能为空!")
    private Integer applyQuantity;

    /** 单价 */
    @ApiModelProperty(name = "returnsPrice", value = "中科退货单价", required = true)
    @NotNull(message = "中科退货单价不能为空!")
    private BigDecimal returnsPrice;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号", required = true)
    @NotNull(message = "行号不能为空!")
    private Integer line;


    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则", required = true)
    private String returnPrinciple;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;
}
