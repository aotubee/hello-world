package com.edc.erp.enumeration;

/**
 * @Description:
 * @Author: ZhangYao
 * @Date: 2023/7/28 16:08
 **/
public enum OrderFreezeEnum {

    UN_FREEZE("unFreeze", "未冻结"),

    FREEZE("freeze", "已冻结"),

    RELEASE("release", "已释放");

    private String key;
    private String value;

    OrderFreezeEnum(String key, String value) {
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
        for (OrderFreezeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
