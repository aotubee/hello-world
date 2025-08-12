package com.edc.erp.directly.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yaojinpeng
 * @since 2022/10/21 16:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnNoticeGoodsIn extends Page implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品code")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 门店code
     */
    @ApiModelProperty(name = "storeCode", value = "门店code")
    private String storeCode;

    private String bizOrgCode;
}
