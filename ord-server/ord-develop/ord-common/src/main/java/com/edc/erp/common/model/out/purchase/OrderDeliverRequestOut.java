package com.edc.erp.common.model.out.purchase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author LZQ
 * @date 2023年01月11日 19:24
 * 查询截止日期出参
 */
@Data
public class OrderDeliverRequestOut implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单状态
     */
    @ApiModelProperty(value = "statusCode",name = "订单状态")
    private String statusCode;

    /**
     * 截单时间
     */
    @ApiModelProperty(value = "truncationDateTime",name = "截单时间")
    private String truncationDateTime;
}
