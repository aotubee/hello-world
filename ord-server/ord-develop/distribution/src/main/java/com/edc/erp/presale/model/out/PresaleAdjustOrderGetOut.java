package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrder;
import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrderDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 预售调整单详情
 */
@Data
public class PresaleAdjustOrderGetOut extends OrdDisPresaleAdjustOrder {
    private static final long serialVersionUID = 1310529117757419445L;
    /**
     * 预售调整单状态描述
     */
    @ApiModelProperty(name = "statusDesc", value = "预售调整单状态描述")
    private String statusDesc;
    /**
     * 预售调整单明细
     */
    @ApiModelProperty(name = "details", value = "预售调整单明细")
    private List<OrdDisPresaleAdjustOrderDetail> details;
}
