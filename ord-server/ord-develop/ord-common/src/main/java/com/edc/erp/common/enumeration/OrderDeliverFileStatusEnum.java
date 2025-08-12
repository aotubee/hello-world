package com.edc.erp.common.enumeration;

/**
 * 单据数据文件执行状态
 */
public enum OrderDeliverFileStatusEnum {
    /**
     * 执行中
     */
    IN_EXECUTION("inExecution", "执行中"),
    /**
     * 已完成
     */
    COMPLETE("complete", "已完成")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OrderDeliverFileStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (OrderDeliverFileStatusEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
