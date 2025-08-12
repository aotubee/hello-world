package com.edc.erp.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


/**
 * @author lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnGoodsAndStoreIn  implements Serializable {

    /**
     * 商品表id
     */
    @ApiModelProperty(name = "id", value = "商品表id")
    private  Integer id;

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
}
