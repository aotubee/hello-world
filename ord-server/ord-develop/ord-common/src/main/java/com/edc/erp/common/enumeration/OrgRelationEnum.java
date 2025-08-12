package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * @author fxw
 */

public enum OrgRelationEnum {
    /**
     * 西安每一天
     */
    XIAN_MYT("67489495", 1003890, "西安每一天", "9000"),
    /**
     * 郑州每一天
     */
    ZHENGZHOU_MYT("57426384", 1003989, "郑州每一天", "5501"),
    /**
     * 包头每一天
     */
    BAOTOU_MYT("1003892", 1003892, "包头每一天", "5100"),
    /**
     * 每一天便利超市连锁有限公司
     */
    EVERYDAY_CHAIN("66383435", 1000000, "每一天便利超市连锁有限公司", "9434"),
    /**
     * 科技公司
     */
    ORDINARY_PROMOTION("67398765", 1016555, "科技公司", "67398765"),
    /**
     * 宝鸡每一天
     */
    BAOJI_MYT("57426385", 1004011, "宝鸡每一天", "5601"),
    /**
     * 每一天西安分公司宇培库
     */
    YU_PEI("9900", 1014031, "每一天西安分公司宇培库", "9900"),
    /**
     * 天岁每一天
     */
    TIANSUI_MYT("459300076", 1003891, "天岁每一天", "5000"),
    ;

    private final String mytCode;
    public final Integer hdCode;
    private final String orgName;
    private final String hdValue;

    OrgRelationEnum(String mytCode, Integer hdCode, String orgName, String hdValue) {
        this.mytCode = mytCode;
        this.hdCode = hdCode;
        this.orgName = orgName;
        this.hdValue = hdValue;
    }

    public static String getMytCode(OrgRelationEnum orgRelationEnum) {
        return orgRelationEnum.mytCode;
    }

    public static Integer getHdCode(OrgRelationEnum orgRelationEnum) {
        return orgRelationEnum.hdCode;
    }

    public static String getOrgName(OrgRelationEnum orgRelationEnum) {
        if (null == orgRelationEnum) {
            return null;
        }
        return orgRelationEnum.orgName;
    }

    public static String getHdValue(OrgRelationEnum orgRelationEnum) {
        if (null == orgRelationEnum) {
            return null;
        }
        return orgRelationEnum.hdValue;
    }

    public static String getOrgNameByHdCode(Integer hdCode) {
        if (null == hdCode) {
            return null;
        }
        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
            if (orgRelationEnum.hdCode.equals(hdCode)) {
                return orgRelationEnum.orgName;
            }
        }
        return "";
    }


//    public static String getMytCodeByHdCode(Integer hdCode) {
//        if (null == hdCode) {
//            return null;
//        }
//        if (XIAN_MYT.hdCode.equals(hdCode)) {
//            return XIAN_MYT.mytCode;
//        }
//        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
//            if (orgRelationEnum.hdCode.equals(hdCode)) {
//                return orgRelationEnum.mytCode;
//            }
//        }
//        return null;
//    }
//    ------------------------注释的这个方法不可放开使用-------------------------------------------
//
//    public static OrgRelationEnum getOrgRelationEnumByHdCode(Integer hdCode) {
//        if (null == hdCode) {
//            return null;
//        }
//        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
//            if (orgRelationEnum.hdCode.equals(hdCode)) {
//                return orgRelationEnum;
//            }
//        }
//        return null;
//    }

    public static OrgRelationEnum getOrgRelationEnumByMytCode(String mytCode) {
        if (null == mytCode) {
            return null;
        }
        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
            if (orgRelationEnum.mytCode.equals(mytCode)) {
                return orgRelationEnum;
            }
        }
        return null;
    }

//    public static Integer getHdCodeByMytCode(String mytCode) {
//        if (StringUtils.isBlank(mytCode)) {
//            return XIAN_MYT.hdCode;
//        }
//        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
//            if (orgRelationEnum.mytCode.equals(mytCode)) {
//                return orgRelationEnum.hdCode;
//            }
//        }
//        return XIAN_MYT.hdCode;
//    }

    public static String getHdValueByHdCode(Integer hdCode) {
        if (null == hdCode) {
            return "";
        }
        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
            if (orgRelationEnum.hdCode.equals(hdCode)) {
                return orgRelationEnum.hdValue;
            }
        }
        return "";
    }

    public static String getHdValueByMytCode(String mytCode) {
        if (StringUtils.isEmpty(mytCode)) {
            return EVERYDAY_CHAIN.hdValue;
        }
        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
            if (orgRelationEnum.mytCode.equals(mytCode)) {
                return orgRelationEnum.hdValue;
            }
        }
        return EVERYDAY_CHAIN.hdValue;
    }

    public String getMytCode() {
        return mytCode;
    }

    public Integer getHdCode() {
        return hdCode;
    }



    public static String getMytCodeByHdValue(String hdValue) {
        if (null == hdValue) {
            return "";
        }
        for (OrgRelationEnum orgRelationEnum : OrgRelationEnum.values()) {
            if (orgRelationEnum.hdValue.equals(hdValue)) {
                return orgRelationEnum.mytCode;
            }
        }
        return "";
    }
}
