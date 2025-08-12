package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName UpdateDistributionEffectiveTimeIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/10/24 11:26
 **/
@Data
public class UpdateDistributionEffectiveTimeIn implements Serializable {
    private static final long serialVersionUID = 3900585679014038066L;

    @ApiModelProperty(name = "distributionOrderId", value = "分货单ID")
    private Long distributionOrderId;

    /**
     * 是否即时生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效(0:否 1是)")
    private Integer isEffectiveImmediately;

    /**
     * 生效时间
     */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

}
