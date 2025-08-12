package com.edc.erp.disdeliveryorder.enumeration;

/**
 * 订单状态枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年5月26日 14:52
 */
public enum ZKReturnStockTypeEnum {

    ZK_TEMPERATURE("0","802"),
    ZK_FREEZE_LOW("1","804"),
     ;



    private String zkStockType;
    private String zkReturnPosition;

    ZKReturnStockTypeEnum(String zkStockType, String zkReturnPosition) {
        this.zkStockType = zkStockType;
        this.zkReturnPosition = zkReturnPosition;
    }

    public String getZkReturnPosition() {
        return zkReturnPosition;
    }

    public String getZkStockType() {
        return this.zkStockType;
    }



    public static String getZkStockTypeByZkReturnPosition(String zkReturnPosition) {
        for (ZKReturnStockTypeEnum ele : values()) {
            if (ele.getZkReturnPosition().equals(zkReturnPosition)) {
                return ele.getZkStockType();
            }
        }
        return null;
    }
}
