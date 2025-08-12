package com.edc.erp.enumeration;

/**
 * 加盟订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum ErpDisMqTagsEnum {

    DIS_BEFORE_CREATE_REQUEST_ORDER("disBeforeCreateRequestOrder", "待订货单状态更新至可创建要货单后消息发送者（要货单创建前）"),
    DIS_AFTER_CREATE_REQUEST_ORDER("disAfterCreateRequestOrder", "要货单创建成功后发送消息"),
    DIS_FIRST_TO_DELIVERY("disFirstToDelivery", "加盟首单铺货拆配销单"),
    DIS_DISTRIBUTION_CREATE_ORDER("disDistributionCreateOrder", "加盟分货单生成订货单"),
    DIS_DELIVERY_TO_DIFFERENCE("disDeliveryToDifference", "加盟配销单收货后生成加盟配销差异单"),
    DIS_PURCHASE_ORDER_TO_ERP("disPurchaseOrderToErp", "采购订单回传配货单"),
    DIS_DELIVERY_TO_DTS("disDeliveryToDts", "DTS配销单数据下发"),
    DIS_DIFFERENCE_ORDER_TO_DTS("disDifferenceOrderToDts", "DTS配销差异单数据下发"),
    DIS_RETURN_TO_DTS("disReturnToDts", "DTS加盟退货单数据下发"),
    WHOLESALE_SHIPMENT_TO_DTS("wholesaleShipmentToDts", "DTS批发出货单数据下发"),
    WHOLESALE_RETURN_TO_DTS("wholesaleReturnToDts", "DTS批发退货单数据下发"),
    DIS_DELIVERY_DTS_TO_ERP("disDeliveryDtsToErp", "DTS配销单数据回传"),
    DIS_RETURN_DTS_TO_ERP("disReturnDtsToErp", "DTS加盟退货单数据回传"),
    DIS_DIFFERENCE_DTS_TO_ERP("disDifferenceDtsToErp", "DTS加盟差异单数据回传"),
    WHOLESALE_ORDER_CALL_BACK("wholesaleOrderCallBack", "DTS批发单数据回传"),
    WHOLESALE_RE_ORDER_CALL_BACK("wholesaleReOrderCallBack", "DTS批发退数据回传"),
    ZK_CREATE_WHOLESALE_SHIPMENT("zKCreateWholesaleShipment", "中科请求创建批发出货单"),
    ZK_CREATE_WHOLESALE_RETURN("zKCreateWholesaleReturn", "中科请求创建批发退货单"),
    ZK_WHOLESALE_SHIPMENT_BACK("zKWholesaleShipmentBack", "中科请求创建批发出货单回传中科"),
    ZK_WHOLESALE_RETURN_BACK("zKWholesaleReturnBack", "中科请求创建批发退货单回传中科"),
    HANDLE_WHOLESALE_SHIPMENT_PURCHASE_BACK("handleWholesaleShipmentPurchaseBack", "中转批发出采购回传"),
    ORDER_DIS_DELIVERY_DATA_FILE("orderDisDeliveryDataFile", "加盟配销单数据文件执行"),


    ;

    private String tag;
    private String value;

    ErpDisMqTagsEnum(String tag, String value) {
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
        for (ErpDisMqTagsEnum ele : values()) {
            if (ele.getTag().equals(tag)) {
                return ele.getValue();
            }
        }
        return null;
    }
}