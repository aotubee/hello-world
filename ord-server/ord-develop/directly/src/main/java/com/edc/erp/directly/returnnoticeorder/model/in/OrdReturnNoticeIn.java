package com.edc.erp.directly.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退货通知单入参查询类
 *
 * @author yaojinpeng
 * @since 2022/10/21 12:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdReturnNoticeIn extends Page implements Serializable {


    /**
     * 主键id
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "主键id")
    private Integer returnNoticeOrderId;


    /**
     * 退货通知单号
     */
    @ApiModelProperty(name = "returnNoticeOrderNo", value = "退货通知单号")
    private String returnNoticeOrderNo;

    /**
     * 创建开始时间
     */
    @ApiModelProperty(name = "beginTime", value = "创建开始时间")
    private String beginTime;

    /**
     * 创建结束时间
     */
    @ApiModelProperty(name = "endTime", value = "创建结束时间")
    private String endTime;

    /**
     * 公司id
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 创建者
     */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     *  状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /**
     * 退货生效开始时间
     */
    @ApiModelProperty(name = "takeEffectBeginTime", value = "退货生效开始时间")
    private String takeEffectBeginTime;

    /**
     * 退货生效结束时间
     */
    @ApiModelProperty(name = "takeEffectEndTime", value = "退货生效结束时间")
    private String takeEffectEndTime;

    /**
     * 退货截止时间
     */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;

}
