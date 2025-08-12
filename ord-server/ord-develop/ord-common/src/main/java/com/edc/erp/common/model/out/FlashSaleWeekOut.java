package com.edc.erp.common.model.out;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 显示抢购星期
 * @since 2022/10/19 18:54
 */
@Data
public class FlashSaleWeekOut implements Serializable {
    private static final long serialVersionUID = -8482888060784944113L;

    /**
     * 星期X（数字）
     */
    private Integer weekNumber;

    /**
     * 星期X（名称）
     */
    private String weekValue;

    /**
     * 限时抢购时间段
     */
    private List<FlashSaleTimeOut> flashSaleTimeOutList;
}
