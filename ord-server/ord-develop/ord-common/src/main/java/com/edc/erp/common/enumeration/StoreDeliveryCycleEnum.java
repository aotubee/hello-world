package com.edc.erp.common.enumeration;

/**
 * 门店配货类型
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年4月23日 18:52
 */
public enum StoreDeliveryCycleEnum {

    DIRECTLY_ODD_DAYS("0001", "直营单日配"),
    DIRECTLY_ALTERNATE_DAYS("0002", "直营双日配"),
    DIRECTLY_DAY_WITH("0003", "直营日配"),
    DIRECTLY_OTHER_DELIVERY("otherDelivery", "直营其他"),
    DIRECTLY_MON_WED_FRIDAY("0004", "直营每周一三五"),
    ODD_DAYS("0005", "单日配"),
    ALTERNATE_DAYS("0006", "双日配"),
    DAY_WITH("0007", "日配"),
    OTHER_DELIVERY("otherDelivery", "其他"),
//    THURSDAY("0009", "每周四"),
    WED_FRIDAY("0009", "每周三五"),
    MON_WED_FRIDAY("0008", "每周一三五");

    private String code;
    private String name;
    StoreDeliveryCycleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (StoreDeliveryCycleEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
