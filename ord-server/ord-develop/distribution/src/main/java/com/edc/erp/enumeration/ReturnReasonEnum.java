package com.edc.erp.enumeration;

/**
 * @description:批发退货原因枚举类
 * @author wld
 * @since 2022/10/28 18:59
 */
public enum ReturnReasonEnum {

    /**
     * 破损
     */
    DAMAGED("damaged", "破损"),
    /**
     * 少发
     */
    UNDER_DELIVERY("underDelivery", "少发"),
    /**
     * 客户不想要
     */
    CUSTOMER_NOT_WANT("customerNotWant","客户不想要"),
    /**
     * 其他
     */
    OTHER("other","其他")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ReturnReasonEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (ReturnReasonEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        for (ReturnReasonEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }
        return null;
    }
}
