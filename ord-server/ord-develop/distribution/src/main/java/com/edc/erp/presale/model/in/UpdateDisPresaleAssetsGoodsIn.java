package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName UpdateDisPresaleAssetsGoodsIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/2 10:22
 **/
@Data
public class UpdateDisPresaleAssetsGoodsIn implements Serializable {
    private static final long serialVersionUID = 1280793977031426464L;

    @ApiModelProperty(name = "presaleActivityId", value = "预售活动ID")
    private Long presaleActivityId;

    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动单号")
    private String presaleActivityNo;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "updateSurplusQuantity", value = "更新数量")
    private BigDecimal updateSurplusQuantity;

    @ApiModelProperty(name = "orderQuantity", value = "订货数量")
    private BigDecimal orderQuantity;

    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;

    @ApiModelProperty(name = "actualLowering", value = "调整/调减")
    private String actualLowering;

    @ApiModelProperty(name = "loginUsername", value = "操作人")
    private String loginUsername;

    @ApiModelProperty(name = "assetsId", value = "资产ID")
    private Long assetsId;

}
