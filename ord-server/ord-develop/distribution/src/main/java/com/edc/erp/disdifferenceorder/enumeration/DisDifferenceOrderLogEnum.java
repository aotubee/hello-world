package com.edc.erp.disdifferenceorder.enumeration;

/**
 * @Description: 配销退货日志
 * @Author: ZhangYao
 * @Date: 2023/3/28 9:27
 **/
public enum DisDifferenceOrderLogEnum {

    ORD_DIS_CHARGE_BACK_AMOUNT("ordDisChargeBackAmount", "配销差异单冲销,返款{0}元"),

    ORD_DIS_APPROVED_BACK_AMOUNT("ordDisApprovedBackAmount", "配销差异单批准,返款{0}元"),

    ORD_DIS_CHARGE_PAY_AMOUNT("ordDisChargePayAmount", "配销差异单冲销,付款{0}元"),

    ORD_DIS_APPROVED_PAY_AMOUNT("ordDisApprovedPayAmount", "配销差异单批准,付款{0}元"),
    ;
    private String name;
    private String values;


    DisDifferenceOrderLogEnum(String name, String values) {
        this.name = name;
        this.values = values;

    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.values;
    }



    public static String getValueByName(String name) {
        for (DisDifferenceOrderLogEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getValue();
            }
        }
        return null;
    }


}
