package com.edc.erp.enumeration;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-31 14:44
 */
public enum ErrorMessageEnum {

    /**
     * 商品已下架
     */
    SKU_SHELVES("商品{0}已下架"),
    /**
     * 商品状态不正确
     */
    SKU_BUS_GATE("商品{0}状态不正确"),
    /**
     * 套餐状态不正确
     */
    PACKAGE("套餐{0}状态不正确"),
    /**
     * 商品不在抢购时间内
     */
    FLASH_SALE("商品{0}不在抢购时间内");

    private String vale;

    ErrorMessageEnum(String vale) {
        this.vale = vale;
    }

    public String getValue() {
        return this.vale;
    }

}
