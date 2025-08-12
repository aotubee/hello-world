//package com.edc.erp.common.enumeration;
//
///**
// * 仓库转温层、订单类型枚举,
// *
// * @author : zhangyao@tseveryday.com
// * @date 2020年4月23日 18:52
// */
//public enum StockCodeEnum {
//
//    NORMAL_TEMPERATURE_WAREHOUSE("901", "roomDistributionCycle"),
//    MATERIEL_TEMPERATURE_WAREHOUSE("911", "roomDistributionCycle"),
//    COLD_CHAIN_FROZEN_WAREHOUSE_STOCK("36", "frozenDistributionCycle"),
//    COLD_CHAIN_REFRIGERATION_WAREHOUSE_STOCK("38", "frozenDistributionCycle"),
//    TIAN_SUI_COLD_CHAIN_REFRIGERATION_WAREHOUSE_STOCK("803", "frozenDistributionCycle"),
//    TIAN_SUI_NORMAL_WAREHOUSE("801", "roomDistributionCycle"),
//    BJ_REFRIGERATION("42","frozenDistributionCycle"),
//    BJ_TEMPERATURE("44","roomDistributionCycle"),
//    BJ_FROZEN("47","frozenDistributionCycle"),
//    XA_FROZEN("30","frozenDistributionCycle"),
//    XA_REFRIGERATION("32","frozenDistributionCycle"),
//    ZZ_FROZEN("20","frozenDistributionCycle"),
//    ZZ_REFRIGERATION("22","frozenDistributionCycle"),
//
//    // -------------------测试增加仓位 begin--------------------
//    ZZ_YP_TEST_NORMAL_WAREHOUSE("998", "roomDistributionCycle"),
////    XIAN_ZQ_TEST_FROZEN_WAREHOUSE("30", "frozenDistributionCycle"),
////    XIAN_ZQ_TEST_REFRIGERATION_WAREHOUSE("32",  "frozenDistributionCycle"),
//    XIAN_ZQ_TEST_NORMAL_WAREHOUSE("41", "roomDistributionCycle"),
//    ZZ_ZQ_TEST_FROZEN_WAREHOUSE("20", "frozenDistributionCycle"),
////    ZZ_ZQ_TEST_REFRIGERATION_WAREHOUSE("22",  "frozenDistributionCycle"),
//    ZZ_ZQ_TEST_NORMAL_WAREHOUSE("25", "roomDistributionCycle"),
//    // -------------------测试增加仓位 end--------------------
//    ;
//
//    private String code;
//    private String value;
//
//    StockCodeEnum(String code, String value) {
//        this.code = code;
//        this.value = value;
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
//
//
//    public static String getValueByCode(String code) {
//        for (StockCodeEnum ele : values()) {
//            if (ele.getCode().equals(code)) {
//                return ele.getValue();
//            }
//        }
//        return null;
//    }
//
//
//
//}
