package com.edc.erp.disdeliveryorder.model.in.zk;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TaskZKWholesaleShipmentSaveIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/5 17:36
 **/
@Data
public class TaskZKWholesaleShipmentSaveIn extends ZKWholesaleShipmentSaveIn implements Serializable {
    private static final long serialVersionUID = 6928441979435624045L;

    private String wholesaleShipmentNo;

    private String bizOrgCode;

    private String shipmentWrh;
}
