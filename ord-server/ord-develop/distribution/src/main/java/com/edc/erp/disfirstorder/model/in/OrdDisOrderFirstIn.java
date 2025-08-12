package com.edc.erp.disfirstorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 *配销铺货单入参
 * @author weichao
 */
@Data
public class OrdDisOrderFirstIn extends Page {

    /** 配销铺货单号 */
    @ApiModelProperty(name = "firstOrderNo", value = "配销铺货单号")
    private String firstOrderNo;

    /** 配销铺货单状态 */
    @ApiModelProperty(name = "firstOrderStatus", value = "配销铺货单状态")
    private String firstOrderStatus;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 审核人 */
    @ApiModelProperty(name = "approver", value = "审核人")
    private String approver;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 创建开始时间
     */
    @ApiModelProperty(name = "createStartTime", value = "创建开始时间")
    private String createStartTime;

    /**
     * 创建结束时间
     */
    @ApiModelProperty(name = "createEndTime", value = "创建结束时间")
    private String createEndTime;

    /**
     * 生效开始时间
     */
    @ApiModelProperty(name = "effectiveStateTime",value = "生效开始时间")
    private String effectiveStateTime;

    /**
     * 生效结束时间
     */
    @ApiModelProperty(name = "effectiveEndTime",value = "生效结束时间")
    private String effectiveEndTime;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 修改者 */
    @ApiModelProperty(name = "updater", value = "修改者")
    private String updater;



}
