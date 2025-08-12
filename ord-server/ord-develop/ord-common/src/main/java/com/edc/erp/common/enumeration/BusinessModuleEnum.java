package com.edc.erp.common.enumeration;

/**
 * Created by Intellij IDEA.
 * User:  LZQ
 * Date:  2022/10/9
 * 业务模块枚举类
 * @author LZQ
 */
public enum BusinessModuleEnum {
    /**
     * 清算业务模块---库存中心
     */
    INVENTORY_CENTER("inventoryCenter","库存中心"),
    /**
     * 清算业务模块---采购中心
     */
    PURCHASE_CENTER("purchaseCenter","采购中心"),
    /**
     * 清算业务模块---销售中心
     */
    MARKET_CENTER("marketCenter","销售中心"),
    ;
    private final String code;
    private final String name;

    BusinessModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
