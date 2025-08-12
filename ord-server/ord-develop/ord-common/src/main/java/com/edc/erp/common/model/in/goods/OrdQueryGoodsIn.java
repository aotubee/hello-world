package com.edc.erp.common.model.in.goods;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName StoreInvQueryIn
 * @Description 商品查询入参
 * @Author ZhangYao
 * @CreateTime 2023/8/21 17:08
 **/
@Data
public class OrdQueryGoodsIn extends Page {

    @ApiModelProperty(value = "门店代码")
    private String storeCode;

    @ApiModelProperty(value = "仓位代码")
    private String stockCode;

    @ApiModelProperty(value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "商品代码集合")
    private List<String> goodsCodes;

    @ApiModelProperty(value = "业务类型")
    private String businessType;
}
