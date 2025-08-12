package com.edc.erp.common.enumeration.warning;

public enum NoticeLogisticsTemplateEnum {

    CUT_PURCHASE_ORDER_SUMMARY("## {0}今日出货数据预测\n" +
            "\n" +
            "### 店型 | 店数 | 散/整:数量 | 金额\n" +
            "\n" +
            "### {1} | {2} | 散件：{3} / 整件：{4} | {5}", "{0}今日出货数据预测");

    private String templateMessage;
    private String key;

    NoticeLogisticsTemplateEnum(String templateMessage, String key) {
        this.templateMessage = templateMessage;
        this.key = key;
    }

    public String getTemplateMessage() {
        return this.templateMessage;
    }

    public String getKey() {
        return this.key;
    }
}
