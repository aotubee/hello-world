package com.edc.erp.directly.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货通知门店入参查询类
 *
 * @author yaojinpeng
 * @since 2022/10/21 16:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdReturnNoticeStoreIn extends Page implements Serializable {

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 退货截止时间
     */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;


    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 退货通知单商品表ID
     */
    @ApiModelProperty(name = "returnNoticeGoodsId", value = "退货通知单商品表ID")
    private Integer returnNoticeGoodsId;

    /**
     * 限量退货时可退数量
     */
    @ApiModelProperty(value = "限量退货时可退数量")
    private BigDecimal qty;

    private String bizOrgCode;

    /**
     * 直营配货退货类型
     */
    @ApiModelProperty(name = "returnType", value = "直营配货退货类型")
    private String returnType;
}
