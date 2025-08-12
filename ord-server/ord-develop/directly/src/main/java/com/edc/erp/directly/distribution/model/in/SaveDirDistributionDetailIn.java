package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName SaveDirDistributionDetailIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/12 16:35
 **/
@Data
public class SaveDirDistributionDetailIn implements Serializable {
    private static final long serialVersionUID = -3698995772024468075L;

    @ApiModelProperty(name = "distributionOrderId", value = "分货单主键ID")
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

    @ApiModelProperty(name = "detailList", value = "分货明细集合")
    private List<OrdDirOrderDistributionDetail> detailList;

    /**
     * 请求来源
     */
    @ApiModelProperty(name = "requestSource", value = "请求来源(1:二维 2三维)")
    private Integer requestSource;

}
