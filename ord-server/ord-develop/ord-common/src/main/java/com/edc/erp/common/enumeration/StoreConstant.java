/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 门店参数常量类
 *
 * @author: lishaobo
 * @date: 2019-08-29
 */
public class StoreConstant {

    /**
     * 省
     */
    public static final String PROVINCE = "省";
    /**
     * 市
     */
    public static final String CITY = "市";
    /**
     * 区
     */
    public static final String COUNTRY = "区";

    /**
     * 门店ID的key
     */
    public static final String STORE_ID = "storeId";

    /**
     * 门店联系人类型的key
     */
    public static final String TYPES = "types";

    /**
     * 用户名的key
     */
    public static final String USERNAME_KEY = "username";

    /**
     * 同步海鼎时的默认用户名
     */
    public static final String USERNAME = "门店中心";

    /**
     * 搜索门店范围（单位为米）
     */
    public static String DISTANCE = "distance";

    /**
     * 其他区域要转换的值
     */
    public static String OTHER_AREA_VALUE = "-";

    /**
     * 其他区域要转换的KEY
     */
    public static String OTHER_AREA_KEY = "otherArea";

    /**
     * 全局类别
     */
    public static String GLOBAL_TYPE = "store";

    /**
     * 门店属性和海鼎门店属性对应关系
     */
    public enum StorePropertyRelations {

        /**
         * 联网连锁店
         */
        DIRECTLY("directly", "联网连锁店"),

        /**
         * 连锁外加盟(销配结算)
         */
        FRANCHISE("franchise", "连锁外加盟(销配结算)");

        private String mytValue;
        private String hdValue;

        StorePropertyRelations(String mytValue, String hdValue) {
            this.mytValue = mytValue;
            this.hdValue = hdValue;
        }

        public String getMytValue() {
            return mytValue;
        }

        public String getHdValue() {
            return hdValue;
        }

        public static String getHdValue(String mytValue) {
            StorePropertyRelations[] storePropertyRelationsList = values();
            for (StorePropertyRelations relations : storePropertyRelationsList) {
                if (relations.getMytValue().equals(mytValue)) {
                    return relations.getHdValue();
                }
            }
            return null;
        }
    }

    /**
     * 门店属性
     */
    public enum StoreProperty {
        /**
         * 直营
         */
        DIRECTLY("directly", "直营"),

        /**
         * 加盟
         */
        FRANCHISE("franchise", "加盟");

        private String mytValue;
        private String hdValue;

        StoreProperty(String mytValue, String hdValue) {
            this.mytValue = mytValue;
            this.hdValue = hdValue;
        }

        public String getMytValue() {
            return mytValue;
        }

        public String getHdValue() {
            return hdValue;
        }

        public static String getHdValue(String mytValue) {
            StoreProperty[] storePropertyList = values();
            for (StoreProperty relations : storePropertyList) {
                if (relations.getMytValue().equals(mytValue)) {
                    return relations.getHdValue();
                }
            }
            return null;
        }
    }

    public enum StoreFormatsEnum {
        /**
         * 便利店
         */
        CONVENIENCE_STORE("convenienceStore", "便利店"),

        /**
         * 生活超市
         */
        LIFE_SUPERMARKETS("lifeSupermarkets", "生活超市"),

        /**
         * 帮便利
         */
        HELP_FACILITATE("helpFacilitate", "帮便利"),

        /**
         * 食杂店
         */
        GROCERY_STORE("groceryStore", "食杂店"),
//        UNMANNED_STORE("unmannedStore", "无人店"),
//        SHARE_THE_SHELVES("shareTheShelves", "共享货架"),
        ;

        private String code;
        private String name;

        StoreFormatsEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getStoreFormatsName(String code) {
            StoreFormatsEnum[] storeFormatsEnums = values();
            for (StoreFormatsEnum formatsEnum : storeFormatsEnums) {
                if (formatsEnum.getCode().equals(code)) {
                    return formatsEnum.getName();
                }
            }
            return null;
        }
    }

    public enum StoreStatusEnum {

        /**
         * 开店中
         */
        NOT_OPENING("notOpening", "开店中"),

        /**
         * 正常营业
         */
        OPEN_AS_USUAL("openAsUsual", "正常营业"),

        /**
         * 暂停营业
         */
        PAUSE_BUSINESS("pauseBusiness", "暂停营业"),

        /**
         * 改造中
         */
        RECONSTRUCTING("reconstructing", "改造中"),

        /**
         * 关店
         */
        SHUT_UP_STORE("shutUpStore", "关店");

        private String code;
        private String name;

        StoreStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
        public static String getHdName(String hdCode) {
            StoreStatusEnum[] storeStatus = values();
            for (StoreStatusEnum storeStatu : storeStatus) {
                if (storeStatu.getCode().equals(hdCode)) {
                    return storeStatu.getName();
                }
            }
            return null;
        }


    }

    /***
     * 公司
     */
    public enum StoreOrgEnum {
        /**
         * 每一天帮便利建设项目部
         */
        BBL("57688065", "每一天帮便利建设项目部"),

        /**
         * 西安每一天
         */
        XAMYT("67489495", "西安每一天"),

        /**
         * 天岁每一天
         */
        TSMYT("459300076", "天岁每一天"),

        /**
         * 郑州每一天
         */
        ZZMYT("377734059", "郑州每一天");

        private String code;
        private String name;

        StoreOrgEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    /***
     * 配送周期
     */
    public enum StoreDeliveryCycleEnum {

        /**
         * 直营单日配
         */
        DIRECTLY_ODD_DAYS("0001", "直营单日配"),

        /**
         * 直营双日配
         */
        DIRECTLY_ALTERNATE_DAYS("0002", "直营双日配"),

        /**
         * 直营日配
         */
        DIRECTLY_DAY_WITH("0003", "直营日配"),

        /**
         * 直营每周一三五
         */
        DIRECTLY_MON_WED_FRIDAY("0004", "直营每周一三五"),

        /**
         * 加盟单日配
         */
        FRANCHISE_ODD_DAYS("0005", "加盟单日配"),

        /**
         * 加盟双日配
         */
        FRANCHISE_ALTERNATE_DAYS("0006", "加盟双日配"),

        /**
         * 加盟日配
         */
        FRANCHISE_DAY_WITH("0007", "加盟日配"),

        /**
         * 加盟每周一三五
         */
        FRANCHISE_MON_WED_FRIDAY("0008", "加盟每周一三五"),

        /**
         * 其他
         */
        OTHER_DELIVERY("otherDelivery", "其他");

        private String code;
        private String name;

        StoreDeliveryCycleEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }


    /**
     * 门店属性和海鼎门店属性对应关系
     */
    public enum OrgRelations {
        /**
         * 66383435
         */
        TS_GROUP("66383435", "9434"),

        /**
         * 67398765
         */
        SCIENCE_AND_TECHNOLOGY("67398765", "5700"),

        /**
         * 67489495
         */
        XIAN_MYT("67489495", "9000"),

        /**
         * 459300076
         */
        TIANSUI_MYT("459300076", "5000"),

        /**
         * 17161279
         */
        LIFE_SUPERMARKETS("17161279", "9000"),

        /**
         * 57426384
         */
        ZHENGZHOU_MYT("57426384", "5501"),

        /**
         * 57426385
         */
        BAOJI_MYT("57426385", "5601"),
        //        bbl("57688065", "9000"),

        /**
         * GS1578535204180
         */
        BAOTOU_MYT("GS1578535204180", "5100");

        private String mytCode;
        private String hdCode;

        OrgRelations(String mytCode, String hdCode) {
            this.mytCode = mytCode;
            this.hdCode = hdCode;
        }

        public String getMytCode() {
            return mytCode;
        }

        public String getHdCode() {
            return hdCode;
        }

        public static String getHdCode(String mytCode) {
            if (StringUtils.isBlank(mytCode)) {
                return TS_GROUP.getHdCode();
            }
            OrgRelations[] orgRelations = values();
            for (OrgRelations relations : orgRelations) {
                if (relations.getMytCode().equals(mytCode)) {
                    return relations.getHdCode();
                }
            }
            return TS_GROUP.getHdCode();
        }
    }

    /**
     * 新老门店字典对应关系
     */
    public enum NewOldRelations {

        /**
         * lifeSupermarkets
         */
        LIFE_SUPERMARKETS("lifeSupermarkets", "03010002"),

        /**
         * convenienceStore
         */
        CONVENIENCE_STORE("convenienceStore", "03010003"),

        /**
         * directly
         */
        DIRECTLY("directly", "03010005"),

        /**
         * franchise
         */
        JOIN("franchise", "03010006"),

        /**
         * franchise
         */
        FRANCHISE("franchise", "03010010"),

        /**
         * openAsUsual
         */
        OPEN_AS_USUAL("openAsUsual", "03010008"),

        /**
         * shutUpStore
         */
        SHUT_UP_STORE("shutUpStore", "03010009");

        private String newValue;
        private String oldValue;

        NewOldRelations(String newValue, String oldValue) {
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public String getOldValue() {
            return oldValue;
        }

        public static String getNewValueByOldValue(String oldValue) {
            NewOldRelations[] newOldRelations = values();
            for (NewOldRelations relations : newOldRelations) {
                if (relations.getOldValue().equals(oldValue)) {
                    return relations.getNewValue();
                }
            }
            return null;
        }
    }

    /**
     * 海鼎温层
     */
    public enum TemperatureLayerEnum {

        /**
         * 常温层
         */
        ROOM_DISTRIBUTION("S0001", "常温层"),

        /**
         * 低温层
         */
        LOW_DISTRIBUTION("S0002", "低温层"),

        /**
         * 冷冻层
         */
        FROZEN_DISTRIBUTION("S0003", "冷冻层");

        private String hdCode;
        private String hdName;

        TemperatureLayerEnum(String hdCode, String hdName) {
            this.hdCode = hdCode;
            this.hdName = hdName;
        }

        public String getHdCode() {
            return hdCode;
        }

        public String getHdName() {
            return hdName;
        }

        public static String getHdName(String hdCode) {
            TemperatureLayerEnum[] orgRelations = values();
            for (TemperatureLayerEnum relations : orgRelations) {
                if (relations.getHdCode().equals(hdCode)) {
                    return relations.getHdName();
                }
            }
            return null;
        }
    }

    /**
     * 门店照片类型
     */
    public enum StoreImgTypeEnum {
        /**
         * 门店布局照片类型
         */
        LAYOUT_IMG_TYPE("storeLayoutImgType", "门店布局照片类型"),

        /**
         * 门店执照照片类型
         */
        CERTIFICATE_IMG_TYPE("storeCertificateImgType", "门店执照照片类型");
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        StoreImgTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 门店布局照片类型
     */
    public enum LayoutImgTypeEnum {

        /**
         * 门店CAD平面图
         */
        STORE_CAD("storeCAD", "门店CAD平面图"),

        /**
         * 门店基盘照片
         */
        STORE_DISC_IMG("storeDiscImg", "门店基盘照片"),

        /**
         * 门头照片
         */
        STORE_HEAD_IMG("storeHeadImg", "门头照片"),

        /**
         * 收银台照片
         */
        CASHIER_DESK_IMG("cashierDeskImg", "收银台照片"),

        /**
         * 仓库照片
         */
        WAREHOUSE_IMG("warehouseImg", "仓库照片"),

        /**
         * 卖场整体布局图
         */
        OVERALL_LAYOUT_IMG("overallLayoutImg", "卖场整体布局图");

        private String code;
        private String name;

        LayoutImgTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getName(String code) {
            LayoutImgTypeEnum[] orgRelations = values();
            for (LayoutImgTypeEnum relations : orgRelations) {
                if (relations.getCode().equals(code)) {
                    return relations.getName();
                }
            }
            return null;
        }
    }

    /**
     * 门店执照照片类型
     */
    public enum CertificateImgEnum {

        /**
         * 门店执照照片
         */
        LICENSE_IMG("storeLicenseImg", "门店执照照片"),

        /**
         * 门店烟草证照照片
         */
        TOBACCO_IMG("storeTobaccoImg", "门店烟草证照照片"),

        /**
         * 门店食品经营许可证照片
         */
        FOOD_IMG("storeFoodImg", "门店食品经营许可证照片"),

        /**
         * 门店医疗器械销售许可证照片
         */
        MEDICAL_SALE_IMG("storeMedicalSaleImg", "门店医疗器械销售许可证照片");

        private String code;
        private String name;

        CertificateImgEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getName(String code) {
            CertificateImgEnum[] orgRelations = values();
            for (CertificateImgEnum relations : orgRelations) {
                if (relations.getCode().equals(code)) {
                    return relations.getName();
                }
            }
            return null;
        }
    }

    /**
     * 门店线上外卖平台类型
     */
    public enum StoreTakeOutEnum {

        /**
         * 美团
         */
        MEI_TUAN("meiTuan", "美团"),

        /**
         * 饿了么
         */
        E_LE_ME("eLeMe", "饿了么"),

        /**
         * 京东到家
         */
        JING_DONG_GO_HOME("jingDongGoHome", "京东到家");
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        StoreTakeOutEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 门店联系人父类型
     */
    public enum StoreParentContatcTypeEnum {

        /**
         * 门店联系人类型
         */
        STORE_CONTACT_TYPE("storeContactType", "门店联系人类型"),

        /**
         * 门店物业联系人类型
         */
        STORE_PROP_CONTACT_TYPE("storePropContactType", "门店物业联系人类型"),

        /**
         * 分租联系人
         */
        STORE_SUBLET_CONTACT_TYPE("storeSubletContactType", "分租联系人");
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        StoreParentContatcTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 门店联系人子类型
     */
    public enum StoreChildContactTypeEnum {

        /**
         * 店长
         */
        OWNER_CONTACT("ownerContact", "店长"),

        /**
         * 店长紧急联系人
         */
        EMERGENCY_CONTACT("emergencyContact", "店长紧急联系人"),

        /**
         * 加盟商
         */
        JOINING_TRADER("joiningTrader", "加盟商"),

        /**
         * 物业
         */
        PROPERTY_CONTACT("propertyContact", "物业"),

        /**
         * 房东
         */
        HOUSE_OWNER("houseOwner", "房东"),

        /**
         * 分租联系人
         */
        LEASE_CONTACT("leaseContact", "分租联系人");
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        StoreChildContactTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 配送中心
     */
    public enum DefaultDistributionCenterEnum {

        /**
         * 每一天到家宇培库
         */
        BBL_YP("57688065", "BBLYP", "每一天到家宇培库"),

        /**
         * 每一天到家宇培库
         */
        YU_PEI_TS("459300076", "BBLYP", "每一天到家宇培库"),

        /**
         * 郑州配送中心
         */
        ZHENG_ZHOU("377734059", "zhengZhou", "郑州配送中心"),

        /**
         * 宝鸡配送中心
         */
        BAO_JI("57426385", "baoJi", "宝鸡配送中心"),

        /**
         * 西安每一天配送中心
         */
        YU_PEI("67489495", "yuPei", "西安每一天配送中心");
        private String orgCode;
        private String code;
        private String name;

        public String getOrgCode() {
            return orgCode;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        DefaultDistributionCenterEnum(String orgCode, String code, String name) {
            this.orgCode = orgCode;
            this.code = code;
            this.name = name;
        }

        public static String getCodeByOrg(String orgCode) {
            DefaultDistributionCenterEnum[] orgRelations = values();
            for (DefaultDistributionCenterEnum relations : orgRelations) {
                if (relations.getOrgCode().equals(orgCode)) {
                    return relations.getCode();
                }
            }
            return null;
        }
    }
}
