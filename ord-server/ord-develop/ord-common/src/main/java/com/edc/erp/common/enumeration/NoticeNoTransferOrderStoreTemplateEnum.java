package com.edc.erp.common.enumeration;

public enum NoticeNoTransferOrderStoreTemplateEnum {

    NO_TRANSFER_ORDER_ONE("## 今日未订中转订单门店通知\n" +
            "\n" +
            "### 今天{0}未订中转商品的门店总数：{1}，明细如下：{2}，请提醒门店及时订货，9点截单！", "今日未订中转订单门店通知"),
    NO_TRANSFER_ORDER_TWO("## 今日未订中转订单门店通知\n" +
            "\n" +
            "### 今天{0}未订中转商品的门店总数：{1}", "今日未订中转订单门店通知");

    private String templateMessage;
    private String key;

    NoticeNoTransferOrderStoreTemplateEnum(String templateMessage, String key) {
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
