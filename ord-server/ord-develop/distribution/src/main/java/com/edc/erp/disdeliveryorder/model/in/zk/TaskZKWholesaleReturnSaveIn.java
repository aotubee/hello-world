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
public class TaskZKWholesaleReturnSaveIn extends ZKWholesaleReturnSaveIn implements Serializable {
    private static final long serialVersionUID = 2302985408786609697L;

    private String wholesaleReturnNo;

    private String bizOrgCode;

    private String storageWrh;
}
