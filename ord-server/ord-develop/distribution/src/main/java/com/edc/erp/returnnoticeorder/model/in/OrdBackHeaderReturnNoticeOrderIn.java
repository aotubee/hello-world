package com.edc.erp.returnnoticeorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台退货通知单表头入参对象
 *
 * @author yaojinpeng
 * @since 2022/10/21 17:17
 */
@Data
public class OrdBackHeaderReturnNoticeOrderIn extends BaseEntity {

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    @NotNull(message = "退货通知单主键不能为空")
    private Integer returnNoticeOrderId;


    /**
     * 退货通知单关联商品主键
     */
    @ApiModelProperty(name = "returnNoticeGoodsId", value = "退货通知单关联商品主键")
    @NotNull(message = "退货通知单关联商品主键不能为空")
    private String returnNoticeGoodsId;
}
