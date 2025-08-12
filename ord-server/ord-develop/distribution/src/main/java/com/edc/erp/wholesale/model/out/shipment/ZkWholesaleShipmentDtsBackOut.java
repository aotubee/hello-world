package com.edc.erp.wholesale.model.out.shipment;

import com.edc.erp.wholesale.model.out.ZkWholesaleDtsBackBaseOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ZkDtsCallBackResultOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 10:42
 **/
@Data
public class ZkWholesaleShipmentDtsBackOut extends ZkWholesaleDtsBackBaseOut implements Serializable {

    private static final long serialVersionUID = -7554033318619337951L;

    @ApiModelProperty(name = "wholesaleShipmentNo", value = "批发出货单号")
    private String wholesaleShipmentNo;

}
