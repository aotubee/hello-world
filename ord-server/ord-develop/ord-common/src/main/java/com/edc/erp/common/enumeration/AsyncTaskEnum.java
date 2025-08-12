package com.edc.erp.common.enumeration;

public enum AsyncTaskEnum {
    DIS_DELIVERY_TO_DTS("disDeliveryToDts", "DTS配销单数据下发"),

    /**
     * 按门店销售优先级顺序
     */
    DIR_DELIVERY_TO_DTS("dirDeliveryToDts", "DTS配货单数据下发"),

    /**
     * 按门店代码的大小顺序
     */
    DIS_DIFFERENCE_ORDER_TO_DTS("disDifferenceOrderToDts", "DTS配销差异单数据下发"),

    DIR_DIFFERENCE_ORDER_TO_DTS("dirDifferenceOrderToDts", "DTS配销差异单数据下发"),

    DIS_RETURN_TO_DTS("disReturnToDts", "DTS配销退货单数据下发"),

    DIR_RETURN_TO_DTS("dirReturnToDts", "DTS直营退货单数据下发"),

    WHOLESALE_SHIPMENT_TO_DTS("wholesaleShipmentToDts", "DTS批发出货单数据下发"),

    WHOLESALE_RETURN_TO_DTS("wholesaleReturnToDts", "DTS批发退货单数据下发"),

    DIS_FIRST_TO_DELIVERY("disFirstToDelivery", "铺货单转配销单"),

    DIR_FIRST_TO_DELIVERY("dirFirstToDelivery", "直营铺货单转配货单"),

    DIS_DISTRIBUTION_TO_REQUEST("disDistributionToRequest", "订货单转集货单"),

    DIR_DISTRIBUTION_TO_REQUEST("dirDistributionToRequest", "订货单转要货单"),

    DIS_REQUEST_TO_DELIVERY("disRequestToDelivery", "集货单生成配销单"),

    DIR_REQUEST_TO_DELIVERY("dirRequestToDelivery", "要货单生成配货单"),

    DIS_DELIVERY_TO_DIFFERENCE("disDeliveryToDifference", "配销单收货生成差异单"),

    DIS_PURCHASE_ORDER_TO_ERP("disPurchaseOrderToErp", "采购订单回传配销单"),

    DIR_PURCHASE_ORDER_TO_ERP("dirPurchaseOrderToErp", "采购订单回传配货单"),

    DIS_DISTRIBUTION_CREATE_ORDER("disDistributionCreateOrder", "配销分货单生成订货单"),

    DIR_DISTRIBUTION_CREATE_ORDER("dirDistributionCreateOrder", "直营分货单生成订货单"),

    DIR_DELIVERY_TO_DIFFERENCE("dirDeliveryToDifference", "配货单收货生成差异单"),

    DIR_DELIVERY_DTS_TO_ERP("dirDeliveryDtsToErp", "DTS配货单数据回传"),

    DIS_DELIVERY_DTS_TO_ERP("disDeliveryDtsToErp", "DTS配销单数据回传"),

    DIR_RETURN_DTS_TO_ERP("dirReturnDtsToErp", "DTS直营退货单数据回传"),

    DIS_RETURN_DTS_TO_ERP("disReturnDtsToErp", "DTS配销退货单数据回传"),

    DIR_DIFFERENCE_DTS_TO_ERP("dirDifferenceDtsToErp", "DTS直营差异单数据回传"),

    DIS_DIFFERENCE_DTS_TO_ERP("disDifferenceDtsToErp", "DTS配销差异单数据回传"),

    DIS_WHOLESALE_DTS_TO_ERP("disWholesaleDtsToErp", "DTS批发单数据回传"),

    DIS_WHOLESALE_RE_DTS_TO_ERP("disWholesaleReDtsToErp", "DTS批发退单数据回传"),

    ORDER_DIR_DELIVERY_DATA_FILE("orderDirDeliveryDataFile", "直营配货单数据文件执行"),

    ORDER_DIS_DELIVERY_DATA_FILE("orderDisDeliveryDataFile", "配销单数据文件执行"),

    DELIVERY_SEND_ZK("deliverySendZk", "配销单推送中科"),

    RETURN_SEND_ZK("returnSendZk", "退货单推送中科"),

    ZK_AUDIT_CALL_BACK("zkAuditCallBack", "中科审核回传"),

    ZK_CONFIRM_CALL_BACK("zkConfirmCallBack", "中科确认回传"),

    ZK_SAVE_WHOLESALE_SHIPMENT("zkSaveWholesaleShipment", "中科请求创建批发出货单"),

    ZK_SAVE_WHOLESALE_RETURN("zkSaveWholesaleReturn", "中科请求创建批发退货单"),

    ZK_WHOLESALE_SHIPMENT_BACK("zkWholesaleShipmentBack", "中科请求创建批发出货单回传中科"),

    ZK_WHOLESALE_RETURN_BACK("zkSaveWholesaleReturnBack", "中科请求创建批发退货单回传中科"),

    WHOLESALE_SHIPMENT_PURCHASE_BACK("wholesaleShipmentPurchaseBack", "批发中转商品发采购回传采购单号"),
    ;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    AsyncTaskEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getNameByCode(String code) {
        for (AsyncTaskEnum biz : values()) {
            if (biz.getCode().equals(code)) {
                return biz.getName();
            }
        }
        return null;
    }
}
