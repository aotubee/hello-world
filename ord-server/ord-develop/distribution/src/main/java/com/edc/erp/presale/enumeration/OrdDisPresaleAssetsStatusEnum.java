package com.edc.erp.presale.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdDisPresaleAssetsStatusEnum {
    /**
     * 已保存
     */
    // SAVED("saved", "已保存"),
    /**
     * 待审核
     */
    EXPIRED("expired", "已过期"),
    /*
    已审核
     */
    NOT_STARTED("notStarted", "未开始"),
    /**
     * 已收货
     */
    ORDERING("ordering", "订货中");

    private String key;
    private String value;

    OrdDisPresaleAssetsStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String key) {
        for (OrdDisPresaleAssetsStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
