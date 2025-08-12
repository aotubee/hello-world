package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直营订单追踪出参
 * @author wei
 */
@Data
public class OrdDirOrderTrackOut  {

    private Integer id;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 订单编号 */
    @ApiModelProperty(name = "orderNo", value = "订单编号")
    private String orderNo;

    /** 订单状态 */
    @ApiModelProperty(name = "orderStatus", value = "订单状态")
    private String orderStatus;

    /** 跟踪日志 */
    @ApiModelProperty(name = "trackLog", value = "跟踪日志")
    private String trackLog;

    /** 业务类型 */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 部门id */
    @ApiModelProperty(name = "deptId", value = "部门id")
    private Integer deptId;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    private String createTimeStr;
}
