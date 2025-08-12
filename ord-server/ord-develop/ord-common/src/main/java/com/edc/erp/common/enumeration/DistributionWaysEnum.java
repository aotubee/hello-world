package com.edc.erp.common.enumeration;

/**
 * @description: 配送方式枚举类
 * @author fxw
 * @since 2022/09/17
 */
public enum DistributionWaysEnum {
    /**
     * 统配
     */
    UNIFIEDDIS("unifiedDis","统配"),

    /**
     * 直送
     */
    DIRECTDELIVERY("directDelivery","直送"),

    /**
     * 中转
     */
    TRANSFER("transfer","中转"),

    /**
     * 直配
     */
    DIRECTMATCHING("directMatching","直配"),

    /**
     * 直流
     */
    DIRECTCURRENT("directCurrent","直流"),

    /**
     * 自营
     */
    AUTOTROPHY("autotrophy","自营"),

    /**
     * 联营
     */
    JOINTOPERATION("jointOperation","联营"),
    ;
    private String type;
    private String name;
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    DistributionWaysEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getNameByType(String type){
        for (DistributionWaysEnum distributionWaysEnum : DistributionWaysEnum.values()) {
            if(distributionWaysEnum.getType().equals(type)){
                return distributionWaysEnum.getName();
            }
        }
        return null;
    }
    public static String getTypeByName(String name){
        for (DistributionWaysEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getType();
            }
        }

        return null;
    }
}
