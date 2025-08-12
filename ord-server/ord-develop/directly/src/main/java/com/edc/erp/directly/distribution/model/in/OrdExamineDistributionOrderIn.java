package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分货单审核入参
 *
 * @author yaojinpeng
 * @since 2022/10/20 12:13
 */
@Data
public class OrdExamineDistributionOrderIn extends BaseEntity implements Serializable{

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(name = "distributionOrderId", value = "分货主键", required = true)
    private Long distributionOrderId;

    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效", required = true, example = "1:立即生效;0:定时生效")
    private Integer isEffectiveImmediately;

    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织code")
    private String bizOrgCode;

    @ApiModelProperty(name = "ordDirOrderDistributionDetails",value = "直营分货门店商品明细集合")
    private List<OrdDirOrderDistributionDetail> ordDirOrderDistributionDetails;
}
