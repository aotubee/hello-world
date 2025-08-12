package com.edc.erp.presale.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 预售调整单类型枚举
 *
 * @author lh
 */
@AllArgsConstructor
@SuppressWarnings("all")
public enum OrdDisPresaleAdjustOrderTypeEnum {
    /**
     * 增加
     */
    ADD("add", "增加"),
    /**
     * 扣减
     */
    REDUCE("reduce", "扣减"),
    ;

    @Getter
    private String code;
    @Getter
    private String name;

    public static String getName(String code) {
        return Arrays.stream(values())
                .filter(ele -> ele.getCode().equals(code))
                .findFirst()
                .map(OrdDisPresaleAdjustOrderTypeEnum::getName)
                .orElse(null);
    }
    public static String getCode(String name) {
        return Arrays.stream(values())
                .filter(ele -> ele.getName().equals(name))
                .findFirst()
                .map(OrdDisPresaleAdjustOrderTypeEnum::getCode)
                .orElse(null);
    }
}
