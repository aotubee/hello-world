package com.edc.erp.presale.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PresaleGoodsFlowPageIn extends Page implements Serializable {
    private static final long serialVersionUID = 5262231974041485016L;
    /**
     * 搜索参数
     */
    @ApiModelProperty(name = "searchParam", value = "搜索参数")
    private String searchParam;
    /**
     * 业务类型
     */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;
    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;
    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;
    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;
    /**
     * 业务单号
     */
    @ApiModelProperty(name = "sourceNo", value = "业务单号")
    private String sourceNo;
    /**
     * 流水开始日期
     */
    @ApiModelProperty(name = "flowStartDate", value = "流水开始日期")
    private String flowStartDate;
    /**
     * 流水结束日期
     */
    @ApiModelProperty(name = "flowEndDate", value = "流水结束日期")
    private String flowEndDate;
    /**
     * 增减类型
     */
    @ApiModelProperty(name = "actualLowering", value = "增减类型")
    private String actualLowering;

    @ApiModelProperty(name = "storeCodeList", value = "门店代码集合")
    private List<String> storeCodeList;
    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;
}
