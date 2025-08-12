package com.edc.erp.disfirstorder.model.out;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 铺货单明细出参
 * @author weichao
 */
@Data
public class OrdDisOrderFirstDetailOut extends OrdDisOrderFirstDetail {
    /**
     * 商品配货规格（1*12）
     */
    @ApiModelProperty(name = "qpcStr", value = "商品配货规格（1*12）")
    private String qpcStr;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockName", value = "仓位")
    private String stockName;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 配货价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配货价")
    private BigDecimal distributionPrice;

    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;


    /**
     * 品类代码
     */
    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;


    @ApiModelProperty(value = "铺货包装数量")
    private BigDecimal distributionPackageNum;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 配货方式
     */
    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    /**
     * 配货规格数量
     */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "商品配货规格数量")
    private BigDecimal distributionSpecificationNum;

    /**
     * 商品配货规格单位（件/箱）
     */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;
}
