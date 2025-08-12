package com.edc.erp.directly.dirrequestorder.model.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 创建要货单发送MQ信息入参
 * @since 2022/10/24 14:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestOrderCreateMqIn implements Serializable {
    private static final long serialVersionUID = -3149408726783755245L;

    private String storeCode;

    private Integer orderCycleId;

    private Long requestOrderId;

    private String bizOrgCode;

    private String requestOrderNo;

    private String loginUsername;

    private Boolean addPushFlag;

    private String auditType;
}
