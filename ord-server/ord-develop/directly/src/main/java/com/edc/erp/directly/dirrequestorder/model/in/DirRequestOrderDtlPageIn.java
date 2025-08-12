package com.edc.erp.directly.dirrequestorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Description
 *
 * @author :weichao
 */
@Data
public class DirRequestOrderDtlPageIn extends Page {

    @ApiModelProperty(name = "requestOrderId", value = "要货单ID")
    @NotNull(message = "要货单ID不能为空")
    private Long requestOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;
}
