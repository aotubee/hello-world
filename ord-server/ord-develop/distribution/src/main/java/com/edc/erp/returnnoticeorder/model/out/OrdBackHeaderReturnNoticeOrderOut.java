package com.edc.erp.returnnoticeorder.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台退货通知单表头出参对象
 *
 * @author yaojinpeng
 * @since 2022/10/21 17:15
 */

@Data
public class OrdBackHeaderReturnNoticeOrderOut extends BaseEntity {

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
     * 退货类型中文
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文")
    private String returnTypeValue;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 可退货总数量
     */
    @ApiModelProperty(name = "totalReturnQuantity", value = "可退货总数量")
    private BigDecimal totalReturnQuantity;

    /**
     * 配销退货原因
     */
    @ApiModelProperty(name = "returnWhy", value = "配销退货原因")
    private String returnWhy;

    private List<OrdReturnNoticeGoodsOut> goodsOutList;

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
     * 备注
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /**
     * 是否立即生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否立即生效")
    private Integer isEffectiveImmediately;

    /**
     * 退货生效时间
     */
    @ApiModelProperty(name = "takeEffectTime", value = "退货生效时间")
    private LocalDateTime takeEffectTime;

    /**
     * 是否已退
     */
    @ApiModelProperty(name = "isReturn", value = "是否已退")
    private Integer isReturn;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;
}
