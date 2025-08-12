package com.edc.erp.directly.dirfirstorder.model.in;

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
public class OrdDirFirstOrderAuditIn implements Serializable {


    private static final long serialVersionUID = -1197839573393057181L;
    @ApiModelProperty(name = "firstOrderId", value = "铺货单ID")
    private Long firstOrderId;

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

    @ApiModelProperty(name = "detailAuditInList", value = "铺货单审核明细")
    public List<OrdDirFirstOrderDetailAuditIn> detailAuditInList;
}
