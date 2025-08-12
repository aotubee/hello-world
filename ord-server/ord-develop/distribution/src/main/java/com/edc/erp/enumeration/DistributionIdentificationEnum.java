package com.edc.erp.enumeration;

/**
 * 客户类型 枚举
 * @author lx
 * @since 2022-10-19 15:10:13
 */
public enum DistributionIdentificationEnum {
    /*
     *批发商
     */
    NORMAL_DISTRIBUTION("normalDistribution","普通分货"),
    /**
     * 加盟商
     */
    PRESALE_DISTRIBUTION("presaleDistribution","预售分货"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    DistributionIdentificationEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (DistributionIdentificationEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (DistributionIdentificationEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
