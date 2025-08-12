package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-07-13 17:44
 */
@Data
public class HandleReturnOrderAuditIn extends BaseEntity {

    @ApiModelProperty(name = "returnOrderNo",required = true, value = "退货单号")
    private String returnOrderNo;

    @ApiModelProperty(name = "auditResult", required = true, value = "审核结果,0=审核通过；1=审核未通过")
    private Integer auditResult;

    @ApiModelProperty(name = "auditTime", required = true, value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(name = "orgCode",required = true, value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "handleReturnOrderDetailAuditList",required = true, value = "退货明细")
    private List<HandleReturnOrderDetailAuditIn> handleReturnOrderDetailAuditList;
}
