package com.edc.erp.common.enumeration;

/**
 * @description: 操作类型枚举
 * @author fxw
 * @since 2022/09/17
 */
public enum OperateLogTypeEnum {

    /**
     * 新增
     */
    SAVE("新增"),

    /**
     * 更新
     */
    UPDATE("更新"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 作废
     */
    INVALID("作废"),

    /**
     * 审核
     */
    APPROVED("审核"),

    /**
     * 生效
     */
    EXECUTED("生效"),

    /**
     * 启用禁用
     */
    ENABLE("启用禁用"),
    /**
     * 中止
     */
    ABORTED("中止"),

    TERMINATED("终止"),

    /**
     * 审核
     */
    ZK_WHOLESALE_SHIPMENT_CREATE("三方单据{0}创批发出货单{1}"),

    ZK_WHOLESALE_SHIPMENT_BACK("中科批发出货单已回传"),

    ZK_WHOLESALE_RETURN_CREATE("三方单据{0}创建批发退货单{1}"),

    ZK_WHOLESALE_RETURN_BACK("中科批发退货单已回传"),

    ;

    private String name;

    OperateLogTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
