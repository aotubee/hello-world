package com.edc.erp.wholesale.model.out.shipment;

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
public class WholesaleDateInfoOut implements Serializable {
    private static final long serialVersionUID = 3794188259770041401L;

    /** 申请数量 */
    @ApiModelProperty(name = "totalApplyQuantity", value = "申请数量")
    private Integer totalApplyQuantity;

    /** 审请金额 */
    @ApiModelProperty(name = "totalApplyAmount", value = "审请金额")
    private BigDecimal totalApplyAmount;

    /** 审核数量 */
    @ApiModelProperty(name = "totalAuditQuantity", value = "审核数量")
    private Integer totalAuditQuantity;

    /** 审核金额 */
    @ApiModelProperty(name = "totalAuditAmount", value = "审核金额")
    private BigDecimal totalAuditAmount;

    /** 出库数量 */
    @ApiModelProperty(name = "totalShipmentQuantity", value = "出库数量")
    private Integer totalShipmentQuantity;

    /** 实际出库金额 */
    @ApiModelProperty(name = "totalPracticalShipmentAmount", value = "实际出库金额")
    private BigDecimal totalPracticalShipmentAmount;
}
