package com.edc.erp.disdeliveryorder.enumeration;

/**
 * 中科仓库
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-25 13:43
 */
public enum ZKPositionEnum {

    ZK_ROOM_TEMPERATURE("801", "常温"),
    ZK_RETURN("802", "常温退仓"),
    ZK_FREEZE_LOW("803",  "低温"),
    ZK_FREEZE_LOW_RETURN("804",  "低温退仓")
    ;

    private String code;
    private String name;

    ZKPositionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }


    public String getName() {
        return this.name;
    }

    public static String getNameByCode(String code) {
        for (ZKPositionEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }

}
