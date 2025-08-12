package com.edc.erp.disdifferenceorder.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 加盟差异单金额汇总信息出参
 *
 * @author lishaobo
 * @since 2023/06/13 15:19
 */
@Data
@ApiModel(value = "DirDiffOrderSummaryOut", description = "加盟差异单金额汇总信息出参")
public class DisDiffOrderSummaryOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请总数量
     */
    @ApiModelProperty(name = "applyTotalQty", value = "申请总数量")
    private BigDecimal applyTotalQty = BigDecimal.ZERO;

    /**
     * 申请总金额
     */
    @ApiModelProperty(name = "applyTotalAmount", value = "申请总金额")
    private BigDecimal applyTotalAmount = BigDecimal.ZERO;

    /**
     * 批准总数量
     */
    @ApiModelProperty(name = "approvalTotalQty", value = "批准总数量")
    private BigDecimal approvalTotalQty = BigDecimal.ZERO;

    /**
     * 批准总金额
     */
    @ApiModelProperty(name = "approvalTotalAmount", value = "批准总金额")
    private BigDecimal approvalTotalAmount = BigDecimal.ZERO;
}