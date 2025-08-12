package com.edc.erp.common.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 限时抢购时间段
 * @since 2022/10/19 18:56
 */
@Data
public class FlashSaleTimeOut implements Serializable {
    private static final long serialVersionUID = 4483319132644878644L;

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;
}
