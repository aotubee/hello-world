package com.edc.erp.directly.dirdeliveryorder.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName NoAuditDeliveryOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/1 9:22
 **/
@Data
public class DirNoAuditDeliveryOrderInfoOut implements Serializable {

    private static final long serialVersionUID = 966297627215154998L;
    private String deliveryOrderNo;

    private String truncationDateTime;
}
