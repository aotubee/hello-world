package com.edc.erp.wholesale.enumeration;

/**
 * 直营订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum PushPurProgressEnum {


    PENDING(1, "待处理"),
    PUSHED(2, "已推送"),
    DONE(3, "已完成")
    ;

    private Integer progress;
    private String value;

    PushPurProgressEnum(Integer progress, String value) {
        this.progress = progress;
        this.value = value;

    }

    public Integer getProgress() {
        return this.progress;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String orderType) {
        for (PushPurProgressEnum ele : values()) {
            if (ele.getProgress().equals(orderType)) {
                return ele.getValue();
            }
        }
        return null;
    }
}