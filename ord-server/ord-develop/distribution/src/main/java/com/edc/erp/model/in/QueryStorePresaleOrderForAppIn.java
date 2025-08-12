package com.edc.erp.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryStorePresaleOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 14:53
 **/
@Data
public class QueryStorePresaleOrderForAppIn extends Page implements Serializable {
    private static final long serialVersionUID = 4954958606933254067L;

    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 创建月份
     */
    @ApiModelProperty(name = "createMonth", value = "创建月份")
    private String createMonth;
}
