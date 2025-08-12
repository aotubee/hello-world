package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName PresaleAssetsDetailForAppOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/11 11:40
 **/
@Data
public class PresaleAssetsDetailForAppOut implements Serializable {
    private static final long serialVersionUID = -4742470274851595459L;

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
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "已订包装数")
    private BigDecimal packageQuantity;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "surplusPackageQuantity", value = "剩余包装数")
    private String surplusPackageQuantity;

    /**
     * 已订货量
     */
    @ApiModelProperty(name = "orderQuantity", value = "已订货量")
    private BigDecimal orderQuantity;

    /**
     * 剩余订货量
     */
    @ApiModelProperty(name = "surplusQuantity", value = "剩余订货量")
    private BigDecimal surplusQuantity;

    @ApiModelProperty(name = "presaleActivityId", value = "活动ID")
    private Long presaleActivityId;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
    private String presaleActivityNo;

    /**
     * 商品主图
     */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;


}
