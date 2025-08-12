package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退货审核通知入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 15:46
 */
@Data
public class HandleReturnOrderReceiveIn extends BaseEntity {

    @ApiModelProperty(name = "directReturnNo", required = true, value = "直退单号")
    private String directReturnNo;

    @ApiModelProperty(name = "auditResult", required = true, value = "审核结果,0=审核通过；1=审核未通过")
    private Integer auditResult;

    @ApiModelProperty(name = "orgCode", required = true, value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "auditTime", required = true, value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(name = "directReturnDetailList", required = true, value = "商品明细集合")
    private List<HandleReturnOrderDetailReceiveIn> directReturnDetailList;
}
