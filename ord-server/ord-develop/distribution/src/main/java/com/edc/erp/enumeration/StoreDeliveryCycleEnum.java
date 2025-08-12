package com.edc.erp.enumeration;

/**
 * 配送周期
 *
 * @author wei
 */
public enum StoreDeliveryCycleEnum {

    /**
     * 直营单日配
     */
    DIRECTLY_ODD_DAYS("0001", "直营单日配"),

    /**
     * 直营双日配
     */
    DIRECTLY_ALTERNATE_DAYS("0002", "直营双日配"),

    /**
     * 直营日配
     */
    DIRECTLY_DAY_WITH("0003", "直营日配"),

    /**
     * 直营每周一三五
     */
    DIRECTLY_MON_WED_FRIDAY("0004", "直营每周一三五"),

    /**
     * 加盟单日配
     */
    FRANCHISE_ODD_DAYS("0005", "加盟单日配"),

    /**
     * 加盟双日配
     */
    FRANCHISE_ALTERNATE_DAYS("0006", "加盟双日配"),

    /**
     * 加盟日配
     */
    FRANCHISE_DAY_WITH("0007", "加盟日配"),

    /**
     * 加盟每周一三五
     */
    FRANCHISE_MON_WED_FRIDAY("0008", "加盟每周一三五"),

    /**
     * 其他
     */
    OTHER_DELIVERY("otherDelivery", "其他");

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
}

