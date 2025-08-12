package com.edc.erp.disdeliveryorder.model.in;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName DirDeliveryOccupyInventoryIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/6 15:47
 **/
@Data
public class DirDeliveryOccupyInventoryIn implements Serializable {
    private static final long serialVersionUID = -3661857630942514539L;

    private String bizOrgCode;

    private String loginUsername;

    private List<Long> deliveryOrderIdList;
}
