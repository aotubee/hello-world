package com.edc.erp.directly.returnnoticeorder.enumeration;

/**
 * @return: 退货通知单类型
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrdReturnNoticeTypeEnum {

    /**
     * 退货通知单类型
     */
    SPECIAL_RETURN("specialReturn", "特退"),
    LIMITED_RETURN("limitedReturn", "限量");

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
