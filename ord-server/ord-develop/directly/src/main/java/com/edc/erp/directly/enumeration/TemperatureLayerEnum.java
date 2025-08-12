//package com.edc.erp.directly.enumeration;
//
///**
// * 仓库转温层、订单类型枚举,
// *
// * @author : zhangyao@tseveryday.com
// * @date 2020年4月23日 18:52
// */
//public enum TemperatureLayerEnum {
//
//    /**
//     * 仓库转温层、订单类型枚举
//     */
//    NORMAL_TEMPERATURE_WAREHOUSE("901", "常温", "12000402", "统配"),
//    MATERIEL_TEMPERATURE_WAREHOUSE("911", "常温", "12000402", "统配"),
//    FROZEN_WAREHOUSE("04", "冷冻", "12000403", "统配"),
//    REFRIGERATION_WAREHOUSE("06", "冷藏", "12000404", "统配"),
//    COLD_CHAIN_FROZEN_WAREHOUSE_STOCK("36", "低温在库", "12000405", "统配"),
//    COLD_CHAIN_REFRIGERATION_WAREHOUSE_STOCK("38", "低温在库", "12000405", "统配"),
//    COLD_CHAIN_REFRIGERATION_WAREHOUSE_TRANSFER("38", "低温中转", "12000406", "中转"),
//    BAOJI_NORMAL_WAREHOUSE("5630", "常温", "12000402", "统配"),
//    BAOJI_LOW_WAREHOUSE("5629", "低温", "12000402", "统配"),
//    TIAN_SUI_COLD_CHAIN_REFRIGERATION_WAREHOUSE_STOCK("803", "低温在库", "12000405", "统配"),
//    TIAN_SUI_COLD_CHAIN_REFRIGERATION_WAREHOUSE_TRANSFER("803", "低温中转", "12000406", "中转"),
//    TIAN_SUI_NORMAL_WAREHOUSE("801", "常温", "12000402", "统配"),
//    BJ_REFRIGERATION("42","冷藏","12000404","统配"),
//    BJ_REFRIGERATION_DEFECT("43", "冷藏残次","12000404","统配"),
//    BJ_TEMPERATURE("44","常温","12000402","统配"),
//    BJ_TEMPERATURE_DEFECT("45", "常温残次","12000402","统配"),
//    BJ_FROZEN("47","冷冻","12000403","统配"),
//    BJ_FROZEN_DEFECT("48", "冷冻残次","12000403","统配"),
//
//    // -------------------测试增加仓位 begin--------------------
//    ZZ_YP_TEST_NORMAL_WAREHOUSE("998", "常温","12000402","统配"),
//    XIAN_ZQ_TEST_FROZEN_WAREHOUSE("30", "冷冻","12000403","统配"),
//    XIAN_ZQ_TEST_REFRIGERATION_WAREHOUSE("32", "冷藏", "12000404", "统配"),
//    XIAN_ZQ_TEST_NORMAL_WAREHOUSE("41", "常温","12000402","统配"),
//    ZZ_ZQ_TEST_FROZEN_WAREHOUSE("20", "冷冻","12000403","统配"),
//    ZZ_ZQ_TEST_REFRIGERATION_WAREHOUSE("22", "冷藏", "12000404", "统配"),
//    ZZ_ZQ_TEST_NORMAL_WAREHOUSE("25", "常温","12000402","统配"),
//    // -------------------测试增加仓位 end--------------------
//    ;
//
//    private String code;
//    private String value;
//    private String orderType;
//    private String distributionType;
//
//    TemperatureLayerEnum(String code, String value, String orderType, String distributionType) {
//        this.code = code;
//        this.value = value;
//        this.orderType = orderType;
//        this.distributionType = distributionType;
//    }
//
//    public String getCode() {
//        return this.code;
//    }
//
//    public String getValue() {
//        return this.value;
//    }
//
//    public String getOrderType() {
//        return this.orderType;
//    }
//
//    public String getDistributionType() {
//        return this.distributionType;
//    }
//
//    public static String getValueByCode(String code) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getCode().equals(code)) {
//                return ele.getValue();
//            }
//        }
//        return null;
//    }
//
//    public static String getOrderTypeByCode(String code) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getCode().equals(code)) {
//                return ele.getOrderType();
//            }
//        }
//        return null;
//    }
//
//    public static TemperatureLayerEnum getTemperatureLayerEnumByOrderType(String orderType) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getOrderType().equals(orderType)) {
//                return ele;
//            }
//        }
//        return null;
//    }
//
//    public static String getValueByCodeAndDistributionType(String code, String distributionType) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getCode().equals(code) && ele.getDistributionType().equals(distributionType)) {
//                return ele.getValue();
//            }
//        }
//        return null;
//    }
//
//    public static String getValueByOrderType(String orderType) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getOrderType().equals(orderType)) {
//                return ele.getValue();
//            }
//        }
//        return null;
//    }
//
//    public static String getOrderTypeByCodeAndDistributionType(String code, String distributionType) {
//        for (TemperatureLayerEnum ele : values()) {
//            if (ele.getCode().equals(code) && ele.getDistributionType().equals(distributionType)) {
//                return ele.getOrderType();
//            }
//        }
//        return null;
//    }
//}
