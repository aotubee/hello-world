package com.edc.erp.common.model.in.unipush;

import lombok.Data;

@Data
public class NotifyPayload {

    /**
     * 消息ID,通过此数据进行覆盖
     */
    private Integer notifyId;
    /**
     * 消息类型
     */
    private String type = "0";
}
