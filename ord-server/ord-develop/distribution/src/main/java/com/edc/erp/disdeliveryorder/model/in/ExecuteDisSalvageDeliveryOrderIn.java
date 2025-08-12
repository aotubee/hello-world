package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行捞单入参
 *
 * @author zy
 */
@Data
public class ExecuteDisSalvageDeliveryOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始截单时间
     */
    @ApiModelProperty(name = "beginTruncationDateTime", value = "开始截单时间")
    private String beginTruncationDateTime;

    /**
     * 结束截单时间
     */
    @ApiModelProperty(name = "endTruncationDateTime", value = "结束截单时间")
    private String endTruncationDateTime;

    /**
     * 捞单状态集合
     */
    @ApiModelProperty(name = "salvageStatusList", value = "捞单状态集合")
    private List<String> salvageStatusList;

    /**
     * 本轮执行时间
     */
    @ApiModelProperty(name = "executeTime", value = "本轮执行时间")
    private LocalDateTime executeTime;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
