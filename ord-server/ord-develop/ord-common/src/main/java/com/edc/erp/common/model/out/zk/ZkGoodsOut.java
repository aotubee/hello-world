package com.edc.erp.common.model.out.zk;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 中科商品映射(ZkGoods)实体类
 *
 * @author makejava
 * @since 2023-01-17 11:31:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "ZkGoods", description = "中科商品映射")
public class ZkGoodsOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /** 商品代码 */
    @ApiModelProperty(name = "skuCode", value = "商品代码")
    private String skuCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 条码 */
    @ApiModelProperty(name = "barCode", value = "条码")
    private String barCode;

    /** 规格 */
    @ApiModelProperty(name = "distributionSpecification", value = "规格")
    private String distributionSpecification;

    /** 零售单位 */
    @ApiModelProperty(name = "specificationUnit", value = "零售单位")
    private String specificationUnit;

    /** 中科商品代码 */
    @ApiModelProperty(name = "zkSkuCode", value = "中科商品代码")
    private String zkSkuCode;

    /** 中科商品名称 */
    @ApiModelProperty(name = "zkGoodsName", value = "中科商品名称")
    private String zkGoodsName;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;


}
