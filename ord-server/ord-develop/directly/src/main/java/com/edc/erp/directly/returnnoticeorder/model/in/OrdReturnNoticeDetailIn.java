package com.edc.erp.directly.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 退货通知单详情入参查询类
 *
 * @author yaojinpeng
 * @since 2022/10/21 12:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdReturnNoticeDetailIn extends Page implements Serializable {

    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
        private Integer returnNoticeOrderId;
    @ApiModelProperty(name = "storeCode", value = "门店code")
        private String storeCode;
    @ApiModelProperty(name = "GoodsCode", value = "商品code")
    private String goodsCode;

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Integer returnOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
