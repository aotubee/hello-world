package com.edc.erp.enumeration;
/**
 * @description:批发冲销相关枚举
 * @author wld
 * @since 2022/11/1 17:00
 */
public enum ReversalEnum {
    /**
     * 冲销单
     */
    IS_REVERSAL_ORDER_TRUE(1,"冲销单"),
    /**
     * 不是冲销单
     */
    IS_REVERSAL_ORDER_FALSE(0,"不是冲销单"),
    /**
     * 被冲销
     */
    IS_REVERSAL_TRUE(1,"被冲销"),
    /**
     * 未被冲销
     */
    IS_REVERSAL_FALSE(0,"未被冲销")
    ;

    private Integer code;
    private String name;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ReversalEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (ReversalEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        for (ReversalEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }
        return null;
    }
}
