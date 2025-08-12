package com.edc.erp.common.enumeration;

/**
 * 允许批发(出货和退货)条件字段
 * @author lx
 * @since 2022-10-21 19:04:06
 */
public enum IsOutReturnEnum {

    /** 允许仓位批发出货 */
    WHOLESALE_BIZ("is_wholesale_out_logc","允许仓位批发出货"),

    /** 允许仓位批发退货 */
    FRANCHISEE_BIZ("is_wholesale_return_logc","允许仓位批发退货"),

    /** 允许批发出货 */
    WHOLESALE("is_wholesale_out","允许批发出货"),

    /** 允许批发退货 */
    FRANCHISEE("is_wholesale_return","允许批发退货"),

    /** 允许集货配销 */
    IS_DIS_DIS_LOGC("is_dis_dis_logc","允许集货配销"),

    /** 允许要货配货 */
    IS_DIS_LOGC("is_dis_logc","允许要货配货")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    IsOutReturnEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (IsOutReturnEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (IsOutReturnEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
