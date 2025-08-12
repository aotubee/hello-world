package com.edc.erp.constant;

/**
 * 系统常量类
 *
 * @author yaojinpeng
 * @since 2022/10/19 11:41
 */
public class DisSystemConstant {

    /**
     * ES系统编号
     */
    public static final String SYSTEM_CODE = "12";

    /**
     * 关闭配销单中转采购任务key
     */
    public static final String CLOSE_AUTO_TRANSFER_DIS_DELIVERY_ORDER_SWITCH_KEY = "closeAutoTransferDisDeliveryorderSwitchKey";

    /**
     * 配销单中转采购任务key
     */
    public static final String AUTO_TRANSFER_DIS_DELIVERYORDER_SWITCH = "ord:switch:autoTransferDisDeliveryorderSwitch";

    public static final String DIS_ORDER_CART = "orderCartKey:";

    /**
     * 收货心跳频率
     */
    public static final Integer DIS_TAKE_DELIVERY_HEART_RATE_TIME = 2;

    /**
     * 重置收货开关key
     */
    public static final String DIS_RESET_TAKE_DELIVERY_SWITCH = "ord:switch:disResetTakeDelivery";

    /**
     * 关闭重置收货任务key
     */
    public static final String DIS_CLOSE_RESET_TAKE_DELIVERY_SWITCH_KEY = "disCloseResetTakeDeliverySwitchKey";

    /**
     * 缓存收货保存数据key
     */
    public static final String DIS_CACHE_TAKE_DELIVERY_ORDER_KEY = "disCacheTakeDeliveryOrder";

    /**
     * 物流收货前缓存收货数据key
     */
    public static final String DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY = "disLogisticsDeliveryBeforeTakeDeliveryOrder";

    /**
     * 自动收货小时
     */
    public static final long AUTO_TAKE_DELIVERY_HOUR = 48;

    /**
     * 关闭自动收货开关
     */
    public static final String DIS_CLOSE_AUTO_TAKE_DELIVERY_SWITCH_KEY = "disCloseAutoTakeDeliverySwitchKey";

    /**
     * 自动收货开关key
     */
    public static final String DIS_AUTO_TAKE_DELIVERY_SWITCH = "ord:switch:disAutoTakeDelivery";

    /**
     * 订单id查询包装商品数量
     */
    public static final String DIS_STORE_TRUNCATION_DATE_TIME_SKU = "disStoreTruncationDateTimeSku:";

    /**
     * 校验同周期订单类型单位时间内重复提交订单
     */
    public static final String CHECK_DIS_ORDER_CYCLE_REPEAT_SUBMIT = "checkDisOrderCycleRepeatSubmit:";

    /**
     * 自动捞单加时key
     */
    public static final String DIS_AUTO_SALVAGE_DELAY_OVERTIME_MINUTES_KEY = "disAutoSalvageDelayOvertimeMinutesKey";

    /**
     * 校验退货单单位时间内重复提交订单
     */
    public static final String CHECK_DIS_RETURN_ORDER_REPEAT_SUBMIT = "checkDisReturnOrderRepeatSubmit:";

    /**
     * 校验配销集货单拆单key
     */
    public static final String CHECK_DIS_REQUEST_ORDER_SPLIT_KEY = "checkDisRequestOrderSplitKey:";

    /**
     * 校验配销集货单拆单过期时间
     */
    public static final Long CHECK_DIS_REQUEST_ORDER_SPLIT_TIME_KEY = 10L;

    /**
     * 校验单位时间内重复收货
     */
    public static final String CHECK_DIS_ORDER_CYCLE_REPEAT_TAKE = "checkDisOrderCycleRepeatTake:";

    public static final String CHECK_DIS_RETURN_ORDER_AUDIT = "checkDisReturnOrderAudit:";

    public static final String CHECK_DIS_RETURN_ORDER_SUBMIT_APP = "checkDisReturnOrderSubmitApp:";

    public static final String CHECK_DIS_DELIVERY_ORDER_AUDIT = "checkDisDeliveryOrderAudit:";

    public static final String CHECK_DIS_ORDER_DELIVERY_IMPORT_ONLY_ONE = "checkDisOrderDeliveryImportOnlyOne:";

    public static final String CHECK_DIS_ORDER_DELIVERY_OVERALL_IMPORT_ONLY_ONE = "checkDisOrderDeliveryOverallImportOnlyOne:";

    public static final String CHECK_DIS_RETURN_IMPORT_ONLY_ONE = "checkDisReturnImportOnlyOne:";

    public static final String CHECK_WHOLESALE_SHIPMENT_IMPORT_ONLY_ONE = "checkWholesaleShipmentImportOnlyOne:";

    public static final String HANDLE_ZK_WHOLESALE_SHIPMENT_KEY = "handleZkWholesaleShipmentKey:";

    public static final String HANDLE_ZK_WHOLESALE_RETURN_KEY = "handleZkWholesaleReturnKey:";

    public static final String DIS_CLOSE_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH_KEY = "disCloseAutoExecutePresaleActivitySwitchKey";

    public static final String DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH = "ord:switch:disAutoExecutePresaleActivity";

    public static final String DIS_CLOSE_AUTO_STOP_PRESALE_ACTIVITY_SWITCH_KEY = "disCloseAutoStopPresaleActivitySwitchKey";

    public static final String DIS_AUTO_STOP_PRESALE_ACTIVITY_SWITCH = "ord:switch:disAutoStopPresaleActivity";

    public static final String DIS_ORDER_PAY = "DIS_ORDER_PAY:";

    public static final String DIS_PRESALE_ORDER_PAY = "DIS_PRESALE_ORDER_PAY:";

    public static final String DIS_CLOSE_PRESALE_ASSETS_SWITCH_KEY = "disClosePresaleAssetsSwitchKey";

    public static final String DIS_PRESALE_ASSETS_SWITCH = "ord:switch:disPresaleAssets";

    public static final String REDIS_DIS_PURCHASE_ORDER_TO_ERP = "redisDisPurchaseOrderToErp:";

    public static final String REDIS_DIS_DELIVERY_DTS_TO_ERP = "redisDisDeliveryDtsToErp:";

    public static final String REDIS_DIS_RETURN_DTS_TO_ERP = "redisDisReturnDtsToErp:";

    public static final String REDIS_DIS_DIFFERENCE_DTS_TO_ERP = "redisDisDifferenceDtsToErp:";

    public static final String REDIS_DIS_HANDLE_DISTRIBUTION_CREATE_ORDER = "redisDisHandleDistributionCreateOrder:";

    public static final String REDIS_DIS_FIRST_TO_DELIVERY = "redisDisFirstToDelivery:";

    public static final String REDIS_DIS_SAVE_DIR_DIFFERENCE = "redisDisSaveDirDifference:";

    public static final long MQ_DELAY_TIME = 1 * 60 * 1000;

    public static final long MQ_DELAY_FAST_TIME = 1 * 20 * 1000;

    public static final String DIS_CHECK_RETURN_RECEIVING = "DisCheckReturnReceiving:";

    public static final String CHECK_DIS_SIGN_DIR_DELIVERY_ORDER = "checkDisSignDirDeliveryOrder:";
}
