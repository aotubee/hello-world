package com.edc.erp.directly.dirdeliveryorder.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName NoSalvageDeliveryOrderOut
 * @Description 未捞单配销单
 * @Author ZhangYao
 * @CreateTime 2023/3/13 17:29
 **/
@Data
public class DirNoSalvageDeliveryOrderOut implements Serializable {

    private static final long serialVersionUID = -8564444677183754504L;
    private Long id;

    private String deliveryOrderNo;
}
