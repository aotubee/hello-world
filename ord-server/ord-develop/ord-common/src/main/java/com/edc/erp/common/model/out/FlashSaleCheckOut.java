package com.edc.erp.common.model.out;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 是否能抢购
 * @since 2022/10/19 18:59
 */
@Data
public class FlashSaleCheckOut implements Serializable {

    /**
     * 是否能抢购
     */
    private Integer isCanBuyFlashSale;

    /**
     * 限时抢购星期
     */
    private List<FlashSaleWeekOut> flashSaleWeekOutList;
}
