package com.edc.erp.common.model.out.fl;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lee
 */
@Data
public class DeliveryNumberVO implements Serializable {

    /**
     * 整件区箱数
     */
    private Integer wholeAreaCases;

    /**
     * 纸箱数
     */
    private Integer cartonNumber;

    /**
     * 物流箱数
     */
    private Integer logisticsBox;
}
