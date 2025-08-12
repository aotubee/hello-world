package com.edc.erp.common.enumeration;

/**
 * 捞单预警类型枚举类型枚举类
 * @author zy
 */
public enum SalvageDeliveryOrderWarningTypeEnum {

    NO_AUTO_SALVAGE("noAutoSalvage", "### {0}\n" +
            "\n" +
            "#### 未执行自动捞单预警：【{1}】距截单时间{2}已过{3}小时未执行捞单占库存，第{4}次预警，请关注！"),
    ;

    private final String type;
    private final String errorMessage;

    SalvageDeliveryOrderWarningTypeEnum(String type, String errorMessage) {
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public String getType() {
        return type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static String getCode(SalvageDeliveryOrderWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.type;
    }

    public static String getName(SalvageDeliveryOrderWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.errorMessage;
    }

    public static String getNameByType(String type) {
        for (SalvageDeliveryOrderWarningTypeEnum upLowerLimitListWarningTypeEnum : values()) {
            if (upLowerLimitListWarningTypeEnum.getType().equals(type)) {
                return upLowerLimitListWarningTypeEnum.getErrorMessage();
            }
        }
        return "";
    }
}
