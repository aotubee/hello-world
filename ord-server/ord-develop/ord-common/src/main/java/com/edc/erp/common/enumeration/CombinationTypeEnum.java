package com.edc.erp.common.enumeration;

/**
 * 商品组合 - 组合类型枚举
 * @author lx
 * @since 2022-10-20 11:03:03
 */
public enum CombinationTypeEnum {
    /** 仓位 */
    POSITION("position","仓位"),
    /** 配送方式 */
    DISTRIBUTION_TYPE("distributionType","配送方式"),
    /** 品类属性 */
    SORT_PROPERTY("sortProperty","品类属性")
    ;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    CombinationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (CombinationTypeEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (CombinationTypeEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
