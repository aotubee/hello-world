package com.edc.erp.common.enumeration;

/**
 * 组织代号枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年7月30日 14:52
 */
public enum OrgCodeConvertEnum {

    XIAN_MYT("2000", "67489495", "西安每一天"),
    TS_MYT("2001", "459300076", "天岁每一天"),
//    BAOJI_MYT("2002", "57426385", "宝鸡每一天"),
    ZHENGZHOU_MYT("2003", "57426384", "郑州每一天"),
    MEI_PIN_TANG("2006", "GS1688784173449", "美品堂商贸公司"),
    MEI_PIN_TANG_ICE_CREAM("2007", "GS1709014298684", "美品堂-冰淇淋项目"),
    ZZ_SUPPLY_CHAIN("2008", "-", "郑州供应链"),
    XA_SUPPLY_CHAIN("2014", "-", "西安供应链"),

    SX_FX_SUPPLY_CHAIN("2015", "-", "陕西蜂行品牌管理有限公司"),
//    NANYANG_MYT("2004", "GS1673438066834", "南阳每一天");
    ;

    private String bizOrgCode;
    private String orgCode;
    private String name;


    public String getOrgCode() {
        return orgCode;
    }

    OrgCodeConvertEnum(String bizOrgCode  , String orgCode, String name) {
        this.bizOrgCode = bizOrgCode;
        this.orgCode = orgCode;
        this.name = name;
    }

    public String getBizOrgCode() {
        return bizOrgCode;
    }

    public String getName() {
        return name;
    }

    public static String getBizOrgCodeByOrgCode(String orgCode){
        for (OrgCodeConvertEnum orgOrderEnum : OrgCodeConvertEnum.values()) {
            if(orgOrderEnum.getOrgCode().equals(orgCode)){
                return orgOrderEnum.getBizOrgCode();
            }
        }
        return null;
    }

    public static String getNameByBizOrgCode(String bizOrgCode){
        for (OrgCodeConvertEnum orgCodeConvertEnum : OrgCodeConvertEnum.values()) {
            if (orgCodeConvertEnum.getBizOrgCode().equals(bizOrgCode)){
                return orgCodeConvertEnum.getName();
            }
        }
        return null;
    }

    public static String getOrgCodeByBizOrgCode(String bizOrgCode){
        for (OrgCodeConvertEnum orgCodeConvertEnum : OrgCodeConvertEnum.values()) {
            if (orgCodeConvertEnum.getBizOrgCode().equals(bizOrgCode)){
                return orgCodeConvertEnum.getOrgCode();
            }
        }
        return null;
    }
}
