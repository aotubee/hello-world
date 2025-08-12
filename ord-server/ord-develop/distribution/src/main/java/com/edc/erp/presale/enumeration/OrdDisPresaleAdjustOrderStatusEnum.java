package com.edc.erp.presale.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 预售调整单状态枚举
 *
 * @author lh
 */
@AllArgsConstructor
@SuppressWarnings("all")
public enum OrdDisPresaleAdjustOrderStatusEnum {
    /**
     * 待审核
     */
    PENDING("pending", "待审核"),
    /**
     * 增加
     */
    APPROVED("approved", "已审核"),
    // /**
    //  * 已冲销
    //  */
    // REVERSED("reversed", "已冲销"),
    /**
     * 已作废
     */
    INVALID("invalid", "已作废"),
    ;

    @Getter
    private String code;
    @Getter
    private String name;

    public static String getName(String code) {
        return Arrays.stream(values())
                .filter(ele -> ele.getCode().equals(code))
                .findFirst()
                .map(OrdDisPresaleAdjustOrderStatusEnum::getName)
                .orElse(null);
    }
}
