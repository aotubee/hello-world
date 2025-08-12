package com.edc.erp.common.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName AdjustStoreExpiryMqIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/29 16:05
 **/
@Data
public class AdjustStoreExpiryMqIn implements Serializable {
    private static final long serialVersionUID = -8211308158764233529L;

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
     * 条码
     */
    @ApiModelProperty(name = "barCode", value = "条码")
    private String barCode;

    /**
     * 商品图片
     */
    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;

    /**
     * 调整原因
     */
    @ApiModelProperty(name = "reason", value = "调整原因")
    private String reason;

    private BigDecimal expiryQty;

    private String reduceExpiry;

    private BigDecimal reduceExpiryQty;

    private String addExpiry;

    private BigDecimal addExpiryQty;

}
