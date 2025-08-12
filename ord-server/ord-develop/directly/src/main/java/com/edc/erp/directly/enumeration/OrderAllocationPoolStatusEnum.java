package com.edc.erp.directly.enumeration;

/**
 * 直营订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum OrderAllocationPoolStatusEnum {

    PENDING("pending", "待处理"),


    DONE("done", "已处理"),

     ;

    private String key;
    private String value;

    OrderAllocationPoolStatusEnum(String key, String value) {
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
        for (OrderAllocationPoolStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}