package com.edc.erp.common.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lishaobo
 * @date 2023-1-6
 */
@Data
public class ActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 活动单号 */
    @ApiModelProperty(name = "activityNo", value = "活动单号")
    private String activityNo;

    /** 状态 */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /** 活动开始时间 */
    @ApiModelProperty(name = "activityStartTime", value = "活动开始时间")
    private LocalDateTime activityStartTime;

    /** 活动结束时间 */
    @ApiModelProperty(name = "activityEndTime", value = "活动结束时间")
    private LocalDateTime activityEndTime;

    /** app促销位置 */
    @ApiModelProperty(name = "appPromotionLocation", value = "app促销位置")
    private String appPromotionLocation;

    /** 活动类型(特价，赠品) */
    @ApiModelProperty(name = "activityType", value = "活动类型(特价，赠品)")
    private String activityType;
}
