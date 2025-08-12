package com.edc.erp.directly.distribution.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量作废订货单入参类
 *
 * @author wanglidong
 * @since 2022/11/24 15:43
 */
@Data
@ApiModel(value = "BatchInvalidOrderIn",description = "批量作废订货单入参类")
public class BatchInvalidOrderIn implements Serializable {

    @ApiModelProperty(value = "分货单号")
    @NotNull(message = "分货单号不能为空")
    private String distributionOrderNo;

    @ApiModelProperty(value = "分货单ID")
    @NotNull(message = "分货单ID不能为空")
    private Long distributionOrderId;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "操作人")
    private String username;

    @ApiModelProperty(value = "作废订货单ID集合")
    @NotNull(message = "作废订货单ID集合不能为空")
    private List<Long> orderIdList;
}