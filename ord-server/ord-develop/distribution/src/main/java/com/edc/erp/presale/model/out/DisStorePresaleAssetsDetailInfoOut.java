package com.edc.erp.presale.model.out;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DisStorePresaleAssetsDetailInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/4 9:26
 **/
@Data
public class DisStorePresaleAssetsDetailInfoOut implements Serializable {
    private static final long serialVersionUID = -8845788209195419051L;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "pageData", value = "分页数据")
    private Page<OrdDisPresaleAssetsDetailOut> pageData;
}
