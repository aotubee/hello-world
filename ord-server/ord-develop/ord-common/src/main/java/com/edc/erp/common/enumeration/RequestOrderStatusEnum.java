package com.edc.erp.common.enumeration;

/**
 * 集货单/要货单状态枚举
 * @author weichao
 */
public enum RequestOrderStatusEnum {
    /**
     * 已拆单
     */
    EXCRETED("excreted", "已拆单"),
    /**
     * 已集单
     */
    COLLECTED("collected", "已集单"),
;
    private String key;
    private String value;

    RequestOrderStatusEnum(String key, String value) {
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
        for (RequestOrderStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }

}
