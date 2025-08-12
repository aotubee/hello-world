package com.edc.erp.common.model.in.fund;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Intellij IDEA.
 * User:  LZQ
 * Date:  2022/9/26
 * 清算入参
 * @author LZQ
 */
@Data
public class RechargeLiquidationIn implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 收入方客户编码 */
    @ApiModelProperty(name = "recipientPrincipalCode", value = "收入方客户编码")
    private String recipientPrincipalCode;
    /** 收入方主体类型 */
    @ApiModelProperty(name = "recipientPrincipalType", value = "收入方主体类型")
    private String recipientPrincipalType;
    /** 清算金额 */
    @ApiModelProperty(name = "liquidationAmount", value = "清算金额")
    @NotNull(message = "金额不能为空")
    private BigDecimal liquidationAmount;
    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    @NotBlank(message = "业务组织不能为空")
    private String bizOrgCode;
    /** 业务单号（清分时用支付流水号） */
    @ApiModelProperty(name = "businessNo", value = "业务单号（清分时用支付流水号）")
    @NotBlank(message = "业务单号不能为空")
    private String businessNo;
    /** 支出方客户编码 */
    @ApiModelProperty(name = "payOrPrincipalCode", value = "支出方客户编码")
    private String payOrPrincipalCode;
    /** 支出方主体类型 */
    @ApiModelProperty(name = "payOrPrincipalType", value = "支出方主体类型")
    private String payOrPrincipalType;
    /** 业务类型 */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    /** 业务模块 业务类型为退款时业务模块必填*/
    @ApiModelProperty(name = "businessModule", value = "业务模块 业务类型为退款时业务模块必填")
    private String businessModule;

    /** 备注 */
    @ApiModelProperty(name = "remark",value = "备注")
    private String remark;

    /** 关联原始业务单号-退款用*/
    @ApiModelProperty(name = "originalBusinessNo", value = "关联原始业务单号-退款用")
    private String originalBusinessNo;

    /** 收支方向:in-收入，out-支出*/
    @ApiModelProperty(name = "direction", value = "收支方向:in-收入，out-支出")
    private String direction;
}
