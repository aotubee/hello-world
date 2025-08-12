package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 保存直营分货单和门店商品明细 入参类
 * @author lx
 * @since 2022-12-06 16:06:36
 */
@Data
public class OrdDirOrderDistIn implements Serializable {

    private static final long serialVersionUID = -1L;

    /** 配货分货单 */
    @ApiModelProperty(name = "ordDirOrderDistribution", value = "配货分货单")
    private OrdDirOrderDistribution ordDirOrderDistribution;

    /** 配货分货单明细 */
    @ApiModelProperty(name = "ordDirOrderDistributionDetails", value = "配货分货门店商品明细集合")
    private List<OrdDirOrderDistributionDetail> ordDirOrderDistributionDetails;
}
