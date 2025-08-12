package com.edc.erp.directly.constant;

public class DirSystemConstant {

    public static final String DIR_ORDER_CART = "orderCartKey:";

    /**
     * 收货心跳频率
     */
    public static final Integer DIR_TAKE_DELIVERY_HEART_RATE_TIME = 2;

    /**
     * 重置收货开关key
     */
    public static final String DIR_RESET_TAKE_DELIVERY_SWITCH = "ord:switch:dirResetTakeDelivery";

    /**
     * 关闭重置收货任务key
     */
    public static final String DIR_CLOSE_RESET_TAKE_DELIVERY_SWITCH_KEY = "dirCloseResetTakeDeliverySwitchKey";

    /**
     * 缓存收货保存数据key
     */
    public static final String DIR_CACHE_TAKE_DELIVERY_ORDER_KEY = "dirCacheTakeDeliveryOrder";

    /**
     * 物流收货前缓存收货数据key
     */
    public static final String DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY = "dirLogisticsDeliveryBeforeTakeDeliveryOrder";


    /**
     * 关闭自动收货开关
     */
    public static final String DIR_CLOSE_AUTO_TAKE_DELIVERY_SWITCH_KEY = "dirCloseAutoTakeDeliverySwitchKey";

    /**
     * 自动收货开关key
     */
    public static final String DIR_AUTO_TAKE_DELIVERY_SWITCH = "ord:switch:dirAutoTakeDelivery";

    /**
     * 订单id查询包装商品数量
     */
    public static final String DIR_STORE_TRUNCATION_DATE_TIME_SKU = "disStoreTruncationDateTimeSku:";

    /**
     * 校验同周期订单类型单位时间内重复提交订单
     */
    public static final String CHECK_DIR_ORDER_CYCLE_REPEAT_SUBMIT = "checkDirOrderCycleRepeatSubmit:";

    /**
     * 自动捞单加时key
     */
    public static final String DIR_AUTO_SALVAGE_DELAY_OVERTIME_MINUTES_KEY = "dirAutoSalvageDelayOvertimeMinutesKey";

    /**
     * 校验退货单单位时间内重复提交订单
     */
    public static final String CHECK_DIR_RETURN_ORDER_REPEAT_SUBMIT = "checkDirReturnOrderRepeatSubmit:";

    /**
     * 校验配货要货单拆单key
     */
    public static final String CHECK_DIR_REQUEST_ORDER_SPLIT_KEY = "checkDirRequestOrderSplitKey:";

    /**
     * 校验配货要货单拆单过期时间
     */
    public static final Long CHECK_DIR_REQUEST_ORDER_SPLIT_TIME_KEY = 10L;

    /**
     * 校验单位时间内重复收货
     */
    public static final String CHECK_DIR_ORDER_CYCLE_REPEAT_TAKE = "checkDirOrderCycleRepeatTake:";

    public static final String CHECK_DIR_RETURN_ORDER_AUDIT = "checkDirReturnOrderAudit:";

    public static final String CHECK_DIR_RETURN_ORDER_SUBMIT_APP = "checkDirReturnOrderSubmitApp:";

    public static final String CHECK_DIR_DELIVERY_ORDER_AUDIT = "checkDirDeliveryOrderAudit:";

    public static final String CHECK_DIR_ORDER_DELIVERY_IMPORT_ONLY_ONE = "checkDirOrderDeliveryImportOnlyOne:";

    public static final String CHECK_DIR_ORDER_DELIVERY_OVERALL_IMPORT_ONLY_ONE = "checkDirOrderDeliveryOverallImportOnlyOne:";

    public static final String CHECK_DIR_RETURN_IMPORT_ONLY_ONE = "checkDirReturnImportOnlyOne:";

    public static final String REDIS_DIR_PURCHASE_ORDER_TO_ERP = "redisDirPurchaseOrderToErp:";

    public static final String REDIS_DIR_DELIVERY_DTS_TO_ERP = "redisDirDeliveryDtsToErp:";

    public static final String REDIS_DIR_RETURN_DTS_TO_ERP = "redisDirReturnDtsToErp:";

    public static final String REDIS_DIR_DIFFERENCE_DTS_TO_ERP = "redisDirDifferenceDtsToErp:";

    public static final String REDIS_DIR_HANDLE_DISTRIBUTION_CREATE_ORDER = "redisDirHandleDistributionCreateOrder:";

    public static final String REDIS_DIR_FIRST_TO_DELIVERY = "redisDirFirstToDelivery:";

    public static final String REDIS_DIR_SAVE_DIR_DIFFERENCE = "redisDirSaveDirDifference:";

    public static final long MQ_DELAY_TIME = 1 * 60 * 1000;

    public static final long MQ_DELAY_FAST_TIME = 1 * 20 * 1000;
    public static final String DIR_CHECK_RETURN_RECEIVING = "DirCheckReturnReceiving:";

    public static final String CHECK_DIR_SIGN_DIR_DELIVERY_ORDER = "checkDirSignDirDeliveryOrder:";

}
