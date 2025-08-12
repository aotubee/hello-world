package com.edc.erp.common.enumeration.warning;

/**
 * 订单预警类型枚举类型枚举类
 * @author w
 */
public enum OrderWarningTypeEnum {

    NOT_TO_REQUEST_ORDER("notToRequestOrder", "### 【{0}-订货单未转单预警】\n" +
            "\n" +
            "#### 以下订货单在{1}截单后未转单成功，请关注！\n" +
            "#### {2}"),
    NOT_TO_DELIVERY_ORDER("notToDeliveryOrder", "### 【{0}-{1}未拆单预警】\n" +
            "\n" +
            "#### 以下{2}在{3}截单后未拆单成功，请关注！\n" +
            "#### {4}"),
    DELIVERY_ORDER_NOT_AUDIT("deliveryOrderNotAudit", "### 【{0}-{1}未审核预警】\n" +
            "\n" +
            "#### 以下{2}在{3}捞单时未审核成功，请关注！\n" +
            "#### {4}"),
    DELIVERY_ORDER_LIMIT_AMOUNT("deliveryOrderLimitAmount", "### 【{0}-{1}要货金额过大预警】\n" +
            "\n" +
            "#### {2}：{3}要货金额({4})超近30天平均要货金额的一倍，请关注！"),

    PAY_BEFORE_DELIVERY_SHIPMENTS_FAIL("payBeforeDeliveryShipmentsFail", "### 【配销单发货扣款失败预警】\n" +
            "\n" +
            "#### 配销单扣款失败，物流已发货，请督导及时处理！配销单：{0}，门店：{1}！"),
    TRANSFER_ORDER_ERROR_CARD("中转配货单生成采购订单异常预警", "### 【中转配货单生成采购订单异常预警】\n" +
            "\n" +
            "#### 时间：{0}\n" +
            "#### {1}"),
    ;

    private final String type;
    private final String errorMessage;

    OrderWarningTypeEnum(String type, String errorMessage) {
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public String getType() {
        return type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static String getCode(OrderWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.type;
    }

    public static String getName(OrderWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.errorMessage;
    }

    public static String getNameByType(String type) {
        for (OrderWarningTypeEnum upLowerLimitListWarningTypeEnum : values()) {
            if (upLowerLimitListWarningTypeEnum.getType().equals(type)) {
                return upLowerLimitListWarningTypeEnum.getErrorMessage();
            }
        }
        return "";
    }
}
