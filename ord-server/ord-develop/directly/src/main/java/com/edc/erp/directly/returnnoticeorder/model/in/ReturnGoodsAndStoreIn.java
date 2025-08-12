package com.edc.erp.directly.returnnoticeorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @return: 退货门店商品入参
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnGoodsAndStoreIn implements Serializable {

    /**
     * 组织商品id
     */
    @ApiModelProperty(name = "id", value = "商品表id")
    private Integer id;

    /**
     * 组织商品id
     */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private  Integer orgGoodsId;
    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品code")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "specification", value = "规格")
    private String specification;

    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    @ApiModelProperty(name = "brand", value = "品牌名称")
    private String brandName;

    /**
     * 门店code
     */
    @ApiModelProperty(name = "storeCode", value = "门店信息")
    private List<ReturnStoreInfoIn> storeInfo;

    private String bizOrgCode;

    @ApiModelProperty(name = "expiry", value = "效期码")
    private String expiry;
}
