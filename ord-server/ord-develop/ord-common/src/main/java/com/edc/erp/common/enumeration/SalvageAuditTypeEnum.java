package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 活动类型枚举类，包含标签
 */

/**
 * @description: 捞单池明细审核类型枚举
 * @author zy
 * @since 2023-02-17
 */
public enum SalvageAuditTypeEnum {

    // 运营端创建配销单         等待手动审核，立即自动审核
    // 加推订货单拆的配销单      立即自动审核
    // 正常截单拆的配销单        等待自动审核
    WAIT_MANUAL_AUDIT("waitManualAudit", "等待手动审核"),
    NOW_AUTO_AUDIT("nowAutoAudit", "立即自动审核"),
    WAIT_AUTO_AUDIT("waitAutoAudit", "等待自动审核");

    private final String code;
    private final String tagName;

    SalvageAuditTypeEnum(String code, String tagName) {
        this.code = code;
        this.tagName = tagName;
    }

    public String getCode() {
        return code;
    }

    public String getTagName() {
        return tagName;
    }
    public static String getCode(SalvageAuditTypeEnum activityTypeEnum) {
        return activityTypeEnum.code;
    }

    public static String getTagName(SalvageAuditTypeEnum activityTypeEnum) {
        return activityTypeEnum.tagName;
    }

    public static String getTagNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (SalvageAuditTypeEnum activityTypeEnum : SalvageAuditTypeEnum.values()) {
            if (activityTypeEnum.code.equals(code)) {
                return activityTypeEnum.tagName;
            }
        }
        return "";
    }
}
