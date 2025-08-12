package com.edc.erp.presale.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName QueryPresaleAssetsPageIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/3 9:07
 **/
@Data
public class QueryPresaleAssetsPageIn extends Page {

    private static final long serialVersionUID = 1656628709133547056L;
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "storeCodeList", value = "门店代码集合")
    private List<String> storeCodeList;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;
}
