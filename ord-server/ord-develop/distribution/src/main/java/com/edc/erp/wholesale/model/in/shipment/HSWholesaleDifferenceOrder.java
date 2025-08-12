package com.edc.erp.wholesale.model.in.shipment;

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
public class HSWholesaleDifferenceOrder extends BackToHsBaseInfo implements Serializable {
    private static final long serialVersionUID = -2197461046441374209L;

//    @ApiModelProperty(name = "returnType",value = "1 退货不退款，2 退货退款（固定值）")
//    private String returnType;

    @ApiModelProperty(name = "remark",value = "备注")
    private String remark;

    @ApiModelProperty(name = "detailList",value = "明细")
    private List<HSWholesaleDifferenceDetail> detailList;
}
