package com.edc.erp.returnnoticeorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

/**
 * 退货通知单导入门店明细
 *
 * @author lishaobo
 * @since 20232/05/13 17:30
 */
@Data
public class ImportNoticeDetailIn {

    @ApiModelProperty(name = "noticeOrderId", value = "退货通知单主键")
    private Integer noticeOrderId;

    @ApiModelProperty(name = "fileId", value = "文件ID")
    @NotEmpty(message = "文件ID不能为空")
    private String fileId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 配销退货类型
     */
    @ApiModelProperty(name = "returnType", value = "配销退货类型")
    private String returnType;

    /**
     * 配销退货原因
     */
    @ApiModelProperty(name = "returnWhy", value = "配销退货原因")
    private String returnWhy;

    /**
     * 退货生效时间
     */
    @ApiModelProperty(name = "takeEffectTime", value = "退货生效时间")
    private LocalDateTime takeEffectTime;

    /**
     * 退货截止时间
     */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 是否立即生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否立即生效")
    private Integer isEffectiveImmediately;

    private String operator;
}
