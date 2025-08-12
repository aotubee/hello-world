//package com.edc.erp.common.enumeration;
//
///**
// * 仓位枚举
// *
// * @author weichao
// */
//public enum OrgStockEnum {
//
//    ROOM_TEMPERATURE("901", "9900","常温"),
//    ROOM_TEMPERATURE_DEFECT("902", "9900", "常温残次"),
//    SUPPLIES("911", "9900", "物料"),
//    FREEZE_LOW("36", "9000","冷冻冷藏"),
//    FREEZE_LOW_DEFECT("37", "9000", "冷链冷冻残次"),
//    LOW_FREEZE("38", "9000","冷冻冷藏"),
//    LOW_FREEZE_DEFECT("39", "9000", "冷链冷藏残次"),
//    CIGARETTE("13", "9900","香烟虚拟"),
//    COLD_CHAIN_DIFFERENCE("40", "9900","冷链差异处理"),
//    BAOJI_ROOM_TEMPERATURE("5630", "5601","常温"),
//    BAOJI_ROOM_TEMPERATURE_DEFECT("5631", "5601", "常温残次"),
//    BAOJI_CIGARETTES("5618", "5601", "宝鸡香烟"),
//    BAOJI_LOW_TEMPERATURE("5629", "5601", "低温"),
//    TIANSUI_ROOM_TEMPERATURE("801", "8888","常温"),
//    TIANSUI_ROOM_TEMPERATURE_DEFECT("802", "8888", "常温残次"),
//    TIANSUI_LOW_TEMPERATURE("803", "8888","低温"),
//    TIANSUI_LOW_TEMPERATURE_DEFECT("804", "8888", "低温残次"),
//    CLAIM_A_PRIZE("804", "8888", "低温残次"),
//
//    // -------------------测试增加仓位 begin--------------------
//
//    // -------------------测试增加仓位 end--------------------
//    ;
//
//    private String code;
//    private String value;
//    private String name;
//
//    OrgStockEnum(String code, String value, String name) {
//        this.code = code;
//        this.value = value;
//        this.name = name;
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
//    public String getName() {
//        return this.name;
//    }
//
//    public static String getValueByCode(String code) {
//        for (OrgStockEnum ele : values()) {
//            if (ele.getCode().equals(code)) {
//                return ele.getValue();
//            }
//        }
//        return null;
//    }
//
//    public static String getNameByCode(String code) {
//        for (OrgStockEnum ele : values()) {
//            if (ele.getCode().equals(code)) {
//                return ele.getName();
//            }
//        }
//        return null;
//    }
//}
