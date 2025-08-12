package com.edc.erp.wholesale.model.in.returns;

import com.edc.erp.wholesale.model.in.shipment.BackToHsBaseInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName HSWholesaleDifferenceOrder
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/11 14:54
 **/
@Data
public class HSWholesaleReturnOrderBackIn extends BackToHsBaseInfo implements Serializable {
    private static final long serialVersionUID = 2141456086599690497L;

    @ApiModelProperty(name = "detailList", value = "明细")
    private List<HSWholesaleReturnDetail> detailList;
}
