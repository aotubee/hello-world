package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分货单生成订货单入参
 * @author lx
 * @since 2022-11-18 11:06:20
 */
@Data
public class DisDistributionInitOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;


    /** 分货单id */
    @ApiModelProperty(name = "distributionOrderId", value = "配销分货单主键")
    private Long distributionOrderId;

    /** 生效时间 */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    /** 操作者 */
    @ApiModelProperty(name = "loginUsername",value = "操作者")
    private String loginUsername;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织")
    private String bizOrgCode;
}
