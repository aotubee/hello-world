package com.edc.erp.distribution.model.in;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 保存配销分货单和门店商品明细 入参类
 * @author lx
 * @since 2022-12-06 16:06:36
 */
@Data
public class OrdDisOrderDistIn implements Serializable {

    private static final long serialVersionUID = -1L;

    /** 配销分货单 */
    @ApiModelProperty(name = "ordDisOrderDistribution", value = "配销分货单")
    private OrdDisOrderDistribution ordDisOrderDistribution;

    /** 配销分货单明细 */
    @ApiModelProperty(name = "ordDisOrderDistributionDetails", value = "配销分货门店商品明细集合")
    private List<OrdDisOrderDistributionDetail> ordDisOrderDistributionDetails;
}
