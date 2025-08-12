package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrdDirDistributionAuditIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/4 15:20
 **/
@Data
public class OrdDisDistributionAuditIn implements Serializable {

    private static final long serialVersionUID = 5517226553951777158L;
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

    @ApiModelProperty(name = "detailAuditInList", value = "分货单审核明细")
    public List<OrdDisDistributionDetailAuditIn> detailAuditInList;
}
