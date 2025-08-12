package com.edc.erp.wholesale.model.out.returns;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName WholesaleTotalOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/3/18 10:19
 **/
@Data
public class WholesaleReturnDateInfoOut implements Serializable {
    private static final long serialVersionUID = 6247786469461776593L;

    /** 申请数量 */
    @ApiModelProperty(name = "totalApplyQuantity", value = "申请数量")
    private Integer totalApplyQuantity;

    /** 审请金额 */
    @ApiModelProperty(name = "totalApplyAmount", value = "审请金额")
    private BigDecimal totalApplyAmount;

    /** 审核数量 */
    @ApiModelProperty(name = "totalCheckQuantity", value = "审核数量")
    private Integer totalCheckQuantity;

    /** 审核金额 */
    @ApiModelProperty(name = "totalCheckAmount", value = "审核金额")
    private BigDecimal totalCheckAmount;

    /** 出库数量 */
    @ApiModelProperty(name = "totalStorageQuantity", value = "实际入库数量")
    private Integer totalStorageQuantity;

    /** 实际出库金额 */
    @ApiModelProperty(name = "totalPracticalStorageAmount", value = "实际入库金额")
    private BigDecimal totalPracticalStorageAmount;
}
