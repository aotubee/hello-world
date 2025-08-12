package com.edc.erp.disdeliveryorder.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName WholesaleShipmentSaveIn
 * @Description 中科批发退货单新增入参
 * @Author ZhangYao
 * @CreateTime 2023/12/1 14:48
 **/
@Data
public class ZKWholesaleShipmentSaveIn implements Serializable {

    private static final long serialVersionUID = 7846897046782954675L;

    @ApiModelProperty(name = "zkWholesaleShipmentIn", value = "出货单单头")
    private ZKWholesaleShipmentIn zkWholesaleShipmentIn;

    @ApiModelProperty(name = "zkWholesaleShipmentDetailInList", value = "出货单明细")
    private List<ZKWholesaleShipmentDetailIn> zkWholesaleShipmentDetailInList;

}
