package com.edc.erp.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QueryStorePresaleOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 16:48
 **/
@Data
public class QueryStorePresaleOrderIn extends Page implements Serializable {
    private static final long serialVersionUID = 4954958606933254067L;

    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 创建月份
     */
    @ApiModelProperty(name = "beginCreateTime", value = "开始创建时间")
    private String beginCreateTime;

    @ApiModelProperty(name = "endCreateTime", value = "结束创建时间")
    private String endCreateTime;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "presaleOrderNo", value = "预售单号")
    private String presaleOrderNo;

    @ApiModelProperty(name = "storeCodeList", value = "门店代码")
    private List<String> storeCodeList;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
