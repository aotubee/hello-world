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
public class ZKWholesaleShipmentDetailIn implements Serializable {
    private static final long serialVersionUID = 5251452989962621984L;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码", required = true)
    @NotBlank(message = "商品代码不能为空!")
    private String goodsCode;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量", required = true)
    @NotNull(message = "申请数量不能为空!")
    private Integer applyQuantity;

    /** 单价 */
    @ApiModelProperty(name = "unitPrice", value = "中科单价", required = true)
    @NotNull(message = "中科单价不能为空!")
    private BigDecimal unitPrice;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号", required = true)
    @NotNull(message = "行号不能为空!")
    private Integer line;

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品（0否1是）,默认0", required = true)
    @NotNull(message = "赠品不能为空!")
    private Integer isGift;

    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则（中科要货单规格字段拆开给退货原则）", required = true)
    @NotBlank(message = "退货原则不能为空!")
    private String returnPrinciple;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注（中科的优惠金额）")
    private String remark;

}
