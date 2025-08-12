package com.edc.erp.returnnoticeorder.enumeration;

/**
 * 退货单类型枚举
 * @author lh
 */
public enum OrdReturnNoticeTypeEnum {
    /**
     * 特退
     */
    SPECIAL_RETURN("specialReturn", "特退"),
    /**
     * 限量
     */
    LIMITED_RETURN("limitedReturn", "限量退货");

    private String key;
    private String value;


    OrdReturnNoticeTypeEnum(String key, String value) {
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
        for (OrdReturnNoticeTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }


}
