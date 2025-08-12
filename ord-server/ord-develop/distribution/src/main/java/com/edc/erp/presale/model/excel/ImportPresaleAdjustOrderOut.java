package com.edc.erp.presale.model.excel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class ImportPresaleAdjustOrderOut implements Serializable {
    private static final long serialVersionUID = -730367052090148388L;
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;
    /**
     * 活动单号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
    private String presaleActivityNo;
    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;
    /**
     * 调整类型
     */
    @ApiModelProperty(name = "adjustType", value = "调整类型")
    private String adjustType;
    /**
     * 调整数量
     */
    @ApiModelProperty(name = "adjustQty", value = "调整数量")
    private BigDecimal adjustQty;
    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;
    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;
    /**
     * 剩余订货量
     */
    @ApiModelProperty(name = "surplusQuantity", value = "剩余订货量")
    private BigDecimal surplusQuantity;
    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;
    /**
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;
    /**
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;
}
