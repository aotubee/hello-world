package com.edc.erp.wholesale.model.in.returns;

import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description:根据商品代码或者商品名称筛查批发退货单详情
 * @author wld
 * @since 2022/10/31 17:32
 */
@Data
public class WholesaleReturnDetailFilterIn extends Page implements Serializable {

    /**
     * 批发退货单主键id
     */
    @ApiModelProperty(name = "wholesaleReturnId",value = "批发退货单主键")
    private Long wholesaleReturnId;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 批发退货单详情集合
     */
    @ApiModelProperty(name = "wholesaleReturnDetailList",value = "批发退货单详情集合")
    private List<WholesaleReturnDetail> wholesaleReturnDetailList;
}
