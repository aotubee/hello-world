package com.edc.erp.directly.enumeration;

/**
 * 直营订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum ErpDirMqTagsEnum {

    DIR_BEFORE_CREATE_REQUEST_ORDER("dirBeforeCreateRequestOrder", "待订货单状态更新至可创建要货单后消息发送者（要货单创建前）"),
    DIR_AFTER_CREATE_REQUEST_ORDER("dirAfterCreateRequestOrder", "要货单创建成功后发送消息"),
    DIR_FIRST_TO_DELIVERY("dirFirstToDelivery", "直营首单铺货拆配货单"),
    DIR_DISTRIBUTION_CREATE_ORDER("dirDistributionCreateOrder", "直营分货单生成订货单"),
    DIR_DELIVERY_TO_DIFFERENCE("dirDeliveryToDifference", "直营配货单收货后生成直营配货差异单"),
    DIR_PURCHASE_ORDER_TO_ERP("dirPurchaseOrderToErp", "采购订单回传配货单"),
    DIR_DELIVERY_TO_DTS("dirDeliveryToDts", "DTS配货单数据下发"),
    DIR_DIFFERENCE_ORDER_TO_DTS("dirDifferenceOrderToDts", "DTS配销差异单数据下发"),
    DIR_RETURN_TO_DTS("dirReturnToDts", "DTS直营退货单数据下发"),
    DIR_DELIVERY_DTS_TO_ERP("dirDeliveryDtsToErp", "DTS配货单数据回传"),
    DIR_RETURN_DTS_TO_ERP("dirReturnDtsToErp", "DTS直营退货单数据回传"),
    DIR_DIFFERENCE_DTS_TO_ERP("dirDifferenceDtsToErp", "DTS直营差异单数据回传"),
    ORDER_DIR_DELIVERY_DATA_FILE("orderDirDeliveryDataFile", "直营配货单数据文件执行"),


    ;

    private String tag;
    private String value;

    ErpDirMqTagsEnum(String tag, String value) {
        this.tag = tag;
        this.value = value;

    }

    public String getTag() {
        return this.tag;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String tag) {
        for (ErpDirMqTagsEnum ele : values()) {
            if (ele.getTag().equals(tag)) {
                return ele.getValue();
            }
        }
        return null;
    }
}