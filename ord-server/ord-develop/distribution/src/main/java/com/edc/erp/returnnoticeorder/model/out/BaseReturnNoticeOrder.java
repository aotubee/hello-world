package com.edc.erp.returnnoticeorder.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退货通知单基础信息
 *
 * @author yaojinpeng
 * @since 2022/10/21 15:37
 */
@Data
public class BaseReturnNoticeOrder extends BaseEntity {

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 退货通知单号
     */
    @ApiModelProperty(name = "returnNoticeOrderNo", value = "退货通知单号")
    private String returnNoticeOrderNo;


    /**
     * 退货类型
     */
    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;

    /**
     * 退货类型中文值
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文值")
    private String returnTypeValue;

    /**
     * 生效时间
     */
    @ApiModelProperty(name = "takeEffectTime", value = "生效时间")
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
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 是否已退
     */
    @ApiModelProperty(name = "isReturn", value = "是否已退")
    private Integer isReturn;

}
