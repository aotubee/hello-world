package com.edc.erp.common.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName ShipmentSendToZkIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 11:58
 **/
@Data
public class ZKWholesaleShipmentBackIn implements Serializable {
    private static final long serialVersionUID = 9193748006111182444L;

    @ApiModelProperty(name = "orderNo", value = "ERP批发出单号")
    private String orderNo;

    @ApiModelProperty(name = "sourceOrderNo", value = "中科原单号")
    private String sourceOrderNo;

    @ApiModelProperty(name = "orderNoDate", value = "时间")
    private String orderNoDate;

    @ApiModelProperty(name = "detailList", value = "明细")
    private List<ZKWholesaleShipmentDetailBackIn> detailList;
}
