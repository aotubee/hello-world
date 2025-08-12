package com.edc.erp.common.enumeration;

/**
 * 单据状态(调用库存用)
 * @author weichao
 */
public enum OrderTypeEnum {

    /**
     * 待审核状态
     */
    PENDING("pending", "待审核"),
    /**
     * 已审核状态
     */
    APPROVED("approved", "已审核"),
    /**
     * 已生效状态
     */
    EXECUTED("executed", "已生效"),
    /**
     * 已作废状态
     */
    INVALID("invalid", "已作废"),
    /**
     * 已完成状态
     */
    FINISH("finish", "已完成"),
    /**
     * 已冲销状态
     */
    COVER("cover", "已冲销"),
    /**
     * 未收货状态
     */
    UNCOLLECTED("uncollected", "未收货"),
    /**
     * 未收货状态
     */
    COLLECTED("collected", "已收货"),
    /**
     * 已拒绝状态
     */
    REFUSE("refuse", "已拒绝"),
    /**
     * 已申请状态
     */
    APPLY("apply", "已申请"),
    /**
     * 已取消
     */
    CANCEL("cancel", "已取消"),
    /**
     * 已保存
     */
    PRESERVE("preserve", "已保存"),
    /**
     * 已发货
     */
    SHIPPED("shipped", "已发货");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (OrderTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
