package com.edc.erp.directly.enumeration;

/**
 * @author fxw
 * @description: 附件类型
 * @since 2022/11/1 15:57
 */
public enum AttachmentTypeEnum {

    /**
     * 附件类型
     */
    SIGN_FOR(1, "签收"),
    TAKE_DELIVERY(2, "收货");

    private Integer key;
    private String value;

    AttachmentTypeEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String key) {
        for (AttachmentTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
