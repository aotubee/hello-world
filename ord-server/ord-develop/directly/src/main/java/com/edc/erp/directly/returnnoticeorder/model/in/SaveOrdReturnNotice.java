package com.edc.erp.directly.returnnoticeorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 保存退货通知单入参
 *
 * @author yaojinpeng
 * @since 2022/10/30 16:43
 */
@Data
public class SaveOrdReturnNotice {

    /**
     * 主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "主键")
    private Integer returnNoticeOrderId;

    /**
     * 直营配货退货类型
     */
    @ApiModelProperty(name = "returnType", value = "直营配货退货类型")
    private String returnType;

    /**
     * 直营配货退货原因
     */
    @ApiModelProperty(name = "returnWhy", value = "直营配货退货原因")
    private String returnWhy;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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
     *是否立即生效
     */
    @ApiModelProperty(name = "returnDeadline", value = "是否立即生效")
    private Integer isEffectiveImmediately;

    /**
     * 参数明细
     */
    private List<ReturnGoodsAndStoreIn> goodsDetailed;

    private String bizOrgCode;
}
