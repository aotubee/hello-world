package com.edc.erp.directly.enumeration;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-31 14:44
 */
public enum DistErrorMessageEnum {

    SKU_SHELVES("商品{0}已下架"),
    SKU_BUS_GATE("商品{0}状态不允许分货"),
    PACKAGE("套餐{0}状态不正确"),
    FLASH_SALE("商品{0}不在抢购时间内");

    private String vale;

    DistErrorMessageEnum(String vale) {
        this.vale = vale;
    }

    public String getValue() {
        return this.vale;
    }

}
