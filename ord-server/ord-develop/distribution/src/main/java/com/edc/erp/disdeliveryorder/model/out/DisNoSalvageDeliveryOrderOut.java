package com.edc.erp.disdeliveryorder.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName NoSalvageDeliveryOrderOut
 * @Description 未捞单配销单
 * @Author ZhangYao
 * @CreateTime 2023/3/13 17:29
 **/
@Data
public class DisNoSalvageDeliveryOrderOut implements Serializable {
    private static final long serialVersionUID = 1882272122774505396L;

    private Long id;

    private String deliveryOrderNo;
}
