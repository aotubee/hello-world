package com.edc.erp.presale.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 预售调整单列表分页查询入参
 */
@Data
public class PresaleAdjustOrderPageIn extends Page implements Serializable {
    private static final long serialVersionUID = 3766390737370484926L;
    /** 门店代码 */
    @ApiModelProperty(name = "storeCode",value = "门店代码")
    private String storeCode;
    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode",value = "商品代码")
    private String goodsCode;
    /** 组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;
    /** 状态 */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;
    /** 调整单单号 */
    @ApiModelProperty(name = "orderNo", value = "调整单单号")
    private String orderNo;
    /** 开始时间 */
    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String startTime;
    /** 结束时间 */
    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String endTime;
    /** 调整类型 */
    @ApiModelProperty(name = "adjustType", value = "调整类型")
    private String adjustType;
    /** 是否冲销单 */
    @ApiModelProperty(name = "isChargeOrder", value = "是否冲销单")
    private String isChargeOrder;
    /** 是否冲销 */
    @ApiModelProperty(name = "isCharge", value = "是否冲销")
    private String isCharge;
    /** 审核开始时间 */
    @ApiModelProperty(name = "approvalStartTime", value = "审核开始时间")
    private String approvalStartTime;
    /** 审核结束时间 */
    @ApiModelProperty(name = "approvalEndTime", value = "审核结束时间")
    private String approvalEndTime;

    @ApiModelProperty(name = "storeCodeList", value = "门店代码集合")
    private List<String> storeCodeList;
    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;
}
