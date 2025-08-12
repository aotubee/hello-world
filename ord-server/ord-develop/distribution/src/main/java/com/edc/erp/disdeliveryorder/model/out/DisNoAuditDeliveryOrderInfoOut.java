package com.edc.erp.disdeliveryorder.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName NoAuditDeliveryOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/1 9:22
 **/
@Data
public class DisNoAuditDeliveryOrderInfoOut implements Serializable {

    private static final long serialVersionUID = 2149039186649252489L;
    private String deliveryOrderNo;

    private String truncationDateTime;
}
