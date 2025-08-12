package com.edc.erp.common.enumeration;

/**
 * @description: 配送方式枚举类
 * @author zy
 */
public enum DeliveryOrderSourceCodeEnum {

    MANUAL("manual","运营端手动"),
    ORDER_CONFIG("orderConfig","订单流配置拆单"),
    FIRST_ORDER("firstOrder","收单铺货拆单"),
    ;
    private String type;
    private String name;
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    DeliveryOrderSourceCodeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getNameByType(String type){
        for (DeliveryOrderSourceCodeEnum distributionWaysEnum : DeliveryOrderSourceCodeEnum.values()) {
            if(distributionWaysEnum.getType().equals(type)){
                return distributionWaysEnum.getName();
            }
        }
        return null;
    }
    public static String getTypeByName(String name){
        for (DeliveryOrderSourceCodeEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getType();
            }
        }

        return null;
    }
}
