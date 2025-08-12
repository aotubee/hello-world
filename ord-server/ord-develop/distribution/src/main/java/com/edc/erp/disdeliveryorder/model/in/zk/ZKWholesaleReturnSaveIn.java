package com.edc.erp.disdeliveryorder.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wld
 */
@Data
public class ZKWholesaleReturnSaveIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批发退货单
     */
    @ApiModelProperty(name = "zkWholesaleReturnIn", value = "批发退货单")
    private ZKWholesaleReturnIn zkWholesaleReturnIn;

    /**
     * 批发退货单明细
     */
    @ApiModelProperty(name = "zkWholesaleReturnDetailInList", value = "批发退货单明细")
    private List<ZKWholesaleReturnDetailIn> zkWholesaleReturnDetailInList;


}