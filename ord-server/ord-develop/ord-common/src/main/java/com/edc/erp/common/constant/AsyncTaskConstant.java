package com.edc.erp.common.constant;

import lombok.Data;

/**
 * 异步任务执行(AsyncTask)常量类
 *
 * @author zhangdong
 * @since 2022-08-30 17:21:08
 */

@Data
public class AsyncTaskConstant {

    /**
     * 异步执行类型
     * 1、DIS_DELIVERY_TO_DTS: DTS配销单数据下发
     * 2、DIS_DIFFERENCE_ORDER_TO_DTS: DTS配销差异单数据下发
     * 3、DIS_RETURN_TO_DTS: DTS配销退货单数据下发
     * 4、WHOLESALE_SHIPMENT_TO_DTS: DTS批发出货单数据下发
     * 5、WHOLESALE_RETURN_TO_DTS: DTS批发退货单数据下发
     */
    public interface Type {
        /**
         * DTS配销单数据下发
         */
        String DIS_DELIVERY_TO_DTS = "disDeliveryToDts";  /**
         * DTS配货单数据下发
         */
        String DIR_DELIVERY_TO_DTS = "dirDeliveryToDts";

        /**
         * DTS配销差异单数据下发
         */
        String DIS_DIFFERENCE_ORDER_TO_DTS = "disDifferenceOrderToDts";

        /**
         * DTS配销差异单数据下发
         */
        String DIR_DIFFERENCE_ORDER_TO_DTS = "dirDifferenceOrderToDts";
        /**
         * DTS配销退货单数据下发
         */
        String DIS_RETURN_TO_DTS = "disReturnToDts";
        /**
         * DTS直营退货单数据下发
         */
        String DIR_RETURN_TO_DTS = "dirReturnToDts";

        /**
         * DTS批发出货单数据下发
         */
        String WHOLESALE_SHIPMENT_TO_DTS = "wholesaleShipmentToDts";

        /**
         * DTS批发退货单数据下发
         */
        String WHOLESALE_RETURN_TO_DTS = "wholesaleReturnToDts";

        /**
         * 铺货单转配销单
         *
         */
        String DIS_FIRST_TO_DELIVERY = "disFirstToDelivery";
        /**
         * 直营铺货单转配货单
         *
         */
        String DIR_FIRST_TO_DELIVERY = "dirFirstToDelivery";
        /**
         * 订货单转集货单
         *
         */
        String DIS_DISTRIBUTION_TO_REQUEST = "disDistributionToRequest";

        /**
         * 订货单转要货单
         *
         */
        String DIR_DISTRIBUTION_TO_REQUEST = "dirDistributionToRequest";

        /**
         * 集货单生成配销单
         */
        String DIS_REQUEST_TO_DELIVERY = "disRequestToDelivery";

        /**
         * 要货单生成配货单
         */
        String DIR_REQUEST_TO_DELIVERY = "dirRequestToDelivery";

        /**
         *  配销单收货生成差异单
         */
        String DIS_DELIVERY_TO_DIFFERENCE = "disDeliveryToDifference";

        /**
         * 采购订单回传配销单
         */
        String DIS_PURCHASE_ORDER_TO_ERP = "disPurchaseOrderToErp";

        /**
         * 采购订单回传配货单
         */
        String DIR_PURCHASE_ORDER_TO_ERP = "dirPurchaseOrderToErp";


//        /**
//         * 配销分货单生成订货单，有返回判断，走同步业务
//         */
//        String DIS_DISTRIBUTION_TO_ORDER = "disDistributionToOrder";

        /**
         * 配销分货单生成订货单（新）
         */
        String DIS_DISTRIBUTION_CREATE_ORDER = "disDistributionCreateOrder";


//        /**
//         * 直营分货单生成订货单，有返回判断，走同步业务
//         */
//        String DIR_DISTRIBUTION_TO_ORDER = "dirDistributionToOrder";

        /**
         * 直营分货单生成订货单（新）
         */
        String DIR_DISTRIBUTION_CREATE_ORDER = "dirDistributionCreateOrder";

        /**
         *  配货单收货生成差异单
         */
        String DIR_DELIVERY_TO_DIFFERENCE = "dirDeliveryToDifference";

        /**
         * DTS配货单数据回传
         */
        String DIR_DELIVERY_DTS_TO_ERP = "dirDeliveryDtsToErp";

        /**
         * DTS配销单数据回传
         */
        String DIS_DELIVERY_DTS_TO_ERP = "disDeliveryDtsToErp";

        /**
         * DTS直营退货单数据回传
         */
        String DIR_RETURN_DTS_TO_ERP = "dirReturnDtsToErp";

        /**
         * DTS配销退货单数据回传
         */
        String DIS_RETURN_DTS_TO_ERP = "disReturnDtsToErp";

        /**
         * DTS直营差异单数据回传
         */
        String DIR_DIFFERENCE_DTS_TO_ERP = "dirDifferenceDtsToErp";

        /**
         * DTS配销差异单数据回传
         */
        String DIS_DIFFERENCE_DTS_TO_ERP = "disDifferenceDtsToErp";

        /**
         * DTS批发单数据回传
         */
        String DIS_WHOLESALE_DTS_TO_ERP = "disWholesaleDtsToErp";

        /**
         * DTS批发退单数据回传
         */
        String DIS_WHOLESALE_RE_DTS_TO_ERP = "disWholesaleReDtsToErp";

        /**
         * 创建配销-捞单池明细
         */
        String DIS_CREATE_SALVAGE_POND_DETAIL = "disCreateSalvagePondDetail";

        /**
         * 直营配货单数据文件执行
         */
        String ORDER_DIR_DELIVERY_DATA_FILE = "orderDirDeliveryDataFile";

        /**
         * 配销单数据文件执行
         */
        String ORDER_DIS_DELIVERY_DATA_FILE = "orderDisDeliveryDataFile";

        /**
         * 配销单推送中科
         */
        String DELIVERY_SEND_ZK = "deliverySendZk";

        /**
         * 退货单推送中科
         */
        String RETURN_SEND_ZK = "returnSendZk";

        /**
         * 中科审核回传
         */
        String ZK_AUDIT_CALL_BACK = "zkAuditCallBack";

        /**
         * 中科确认回传
         */
        String ZK_CONFIRM_CALL_BACK = "zkConfirmCallBack";

        /**
         * 配销单占用库存（无捞单池）
         */
        String DIS_DELIVERY_OCCUPY_INV = "disDeliveryOccupyInv";

        /**
         * 中科请求创建批发出货单
         */
        String ZK_SAVE_WHOLESALE_SHIPMENT = "zkSaveWholesaleShipment";

        /**
         * 中科请求创建批发退货单
         */
        String ZK_SAVE_WHOLESALE_RETURN = "zkSaveWholesaleReturn";

        /**
         * 中科请求创建批发出货单回传中科
         */
        String ZK_WHOLESALE_SHIPMENT_BACK = "zkWholesaleShipmentBack";

        /**
         * 中科请求创建批发退货单回传中科
         */
        String ZK_WHOLESALE_RETURN_BACK = "zkSaveWholesaleReturnBack";

        /**
         * 批发中转商品发采购回传采购单号
         */
        String WHOLESALE_SHIPMENT_PURCHASE_BACK = "wholesaleShipmentPurchaseBack";
    }

    /**
     * 异步执行状态
     * 1、未执行：unexecuted
     * 2、成功：SUCCESS
     * 3、异常：ABNORMAL
     * 4、失败：FAIL
     */
    public interface ExecStatus{
        String UNEXECUTED = "unexecuted";
        String SUCCESS = "success";
        String ABNORMAL = "abnormal";
        String FAIL = "fail";
    }
}
