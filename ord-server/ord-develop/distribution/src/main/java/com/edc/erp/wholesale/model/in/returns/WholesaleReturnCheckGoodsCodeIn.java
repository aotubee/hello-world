package com.edc.erp.wholesale.model.in.returns;

import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
/**
 * 校验商品代码入参
 *
 * @author wanglidong
 * @since 2022/11/3 16:48
 */
@Data
public class WholesaleReturnCheckGoodsCodeIn implements Serializable {

    /**
     * 客户代码
     */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /**
     * 仓位id
     */
    @ApiModelProperty(name = "stockId", value = "仓位id")
    private Integer stockId;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 商品详情对象
     */
    private WholesaleReturnDetail wholesaleReturnDetail;
}
