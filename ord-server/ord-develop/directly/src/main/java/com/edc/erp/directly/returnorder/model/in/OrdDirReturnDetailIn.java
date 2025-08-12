package com.edc.erp.directly.returnorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分页查询退货单明细入参
 *
 * @author yaojinpeng
 * @since 2022/10/27 22:28
 */
@Data
public class OrdDirReturnDetailIn extends Page {

    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Integer returnOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "goodsCode", value = "商品code")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "returnOrderDetailId", value = "退货单通知单号")
    private String  returnNoticeNo;

}
