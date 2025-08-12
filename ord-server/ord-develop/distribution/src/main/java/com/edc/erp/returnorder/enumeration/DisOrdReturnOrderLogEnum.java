package com.edc.erp.returnorder.enumeration;

/**
 * @Description: 配销退货日志
 * @Author: ZhangYao
 * @Date: 2023/3/28 9:27
 **/
public enum DisOrdReturnOrderLogEnum {

    ORD_DIS_RETURN_PROCESSED_BACK_AMOUNT("ordDisReturnProcessedBackAmount", "配销退货单已收货,返款{0}元"),
    ORD_DIS_RETURN_PROCESSED_PAY_AMOUNT("ordDisReturnProcessedPayAmount", "配销退货单已冲销,付款{0}元"),

    ORD_DIS_RETURN_EMPTY_OTHER_GOODS("ordDisReturnEmptyOtherGoods", "配销退货单整单无映射"),

    ORD_DIS_APP_SAVE("ordDisAppSave", "门店保存退货单"),

    ORD_DIS_APP_SUBMIT("ordDisAppSubmit", "门店提交退货单"),
    ;
    private String name;
    private String values;


    DisOrdReturnOrderLogEnum(String name, String values) {
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
        for (DisOrdReturnOrderLogEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getValue();
            }
        }
        return null;
    }


}
