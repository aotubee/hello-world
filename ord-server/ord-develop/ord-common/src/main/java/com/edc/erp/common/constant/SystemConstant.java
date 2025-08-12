package com.edc.erp.common.constant;


import com.edc.erp.common.enumeration.OrgCodeConvertEnum;

/**
 * 系统常量类
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年05月05日 18:56
 */
public class SystemConstant {

    /**
     * ES系统编号
     */
    public static final String SYSTEM_CODE = "ord";


    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = "ordStart";

    /**
     * 系统
     */
    public static final String SYSTEM_USER = "系统自动";

    /**
     * 显示抢购商品redis缓存key
     */
    public static final String FLASH_SALE_REDIS_KEY = "flashSaleRedis";

    /**
     * 报损香烟大分类
     */
    public static final String REPORTED_LOSS_ORDER_CIGARETTE_BIG_SORT = "01";

    /**
     * 报损其他大分类
     */
    public static final String OTHER_BIG_SORT = "other";

    /**
     * 冒号
     *
     * @author: lishaobo
     * @date: 2020-09-08 10:08
     */
    public static final String COLON = ":";

    /**
     * 逗号
     *
     * @author: lishaobo
     * @date: 2020-09-08 10:08
     */
    public static final String COMMA = ",";

    /**
     * 短横线
     *
     * @author: lishaobo
     * @date: 2023-01-17 10:08
     */
    public static final String SHORT_LINE = "-";

    /**
     * 等号
     */
    public static final String WAIT = " = ";

    /**
     * 无权访问
     */
    public static final String HTTP_UNAUTHORIZED = "401";

    /**
     * 西安组织中转商品发采购任务执行时间
     */
    public static final String XIAN_EXECUTE_TIME = "10:00:00";

    /**
     * 天岁组织中转商品发采购任务执行时间
     */
    public static final String TS_EXECUTE_TIME = "10:00:00";

    /**
     * 郑州组织中转商品发采购任务执行时间
     */
    public static final String ZHENGZHOU_EXECUTE_TIME = "07:30:00";



    /**
     * 常温配送周期
     */
    public final static String ROOM_DISTRIBUTION_CYCLE = "roomDistributionCycle";

    /**
     * 冷冻商品配送周期
     */
    public final static String FROZEN_DISTRIBUTION_CYCLE = "frozenDistributionCycle";

    /**
     * 按周内星期勾选配送
     */
    public final static String DELIVERY_BY_DAY = "deliveryByDay";

    public final static String SYSTEM_MAINTENANCE = "systemMaintenance";

    /**
     * 订单类型中转code码
     */
    public final static String ORDER_TYPE_CONFIG_TRANSFER = "07,08,09,13";

    /**
     * 上下限跑货定时器key
     */
    public static final String REPLENISHMENT_ORDER_SWITCH = "order:switch:replenishmentOrder";
    /**
     * 上下限跑货定时器key
     */
    public static final String DIR_REPLENISHMENT_ORDER_SWITCH = "order:switch:dirReplenishmentOrder";

    /**
     * 订单任务流转定时器key
     */
    public static final String ASYNC_PUSH_TASK = "order:switch:asyncPushTask";

    public static final String ASYNC_PUSH_TASK_ORG = "order:switch:asyncPushTaskOrg";

    /**
     * 配销退货通知单生效定时器key
     */
    public static final String DIS_RETURN_NOTICE_ORDER_SWITCH = "order:switch:disReturnNoticeOrder";

    /**
     * 直营退货通知单生效定时器key
     */
    public static final String DIR_RETURN_NOTICE_ORDER_SWITCH = "order:switch:dirReturnNoticeOrder";
    /**
     * 配销铺货单单生效定时器key
     */
    public static final String DIS_FIRST_ORDER_SWITCH = "order:switch:disFirstOrder";
    /**
     * 直营铺货单单生效定时器key
     */
    public static final String DIR_FIRST_ORDER_SWITCH = "order:switch:dirFirstOrder";



    public static final String[] BIZORGCODES = new String[]{OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode(),
            OrgCodeConvertEnum.TS_MYT.getBizOrgCode(), OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode()};

    /** 批量插入 初始500条 */
    public final static int PAGE_SIZE = 500;

    /** 定时任务超时锁  时间 */
    public final static long LOCK_TIME_OUT = 5 * 60 * 1000;

    /** 上下限跑货定时任务超时锁  时间 */
    public final static long UL_LOCK_TIME_OUT = 3 * 60 * 60 * 1000;


    /** 铺货单定时任务超时锁  时间 */
    public final static long FIRST_LOCK_TIME_OUT = 3 * 60 * 1000;

    /**
     * 购物车上限值
     */
    public static final Integer MAX_ORDER_CART_COUNT = 500;

    /**
     * 配销分货单自动生效定时任务 开关
     */
    public static final String CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY = "closeAutoOrdDistOrderSwitchKey";

    /**
     *配销分货单自动生效定时任务 key
     */
    public static final String AUTO_ORD_DIST_ORDER_SWITCH = "autoOrdDistOrderSwitch";

    /**
     * 直营分货单自动生效定时任务 开关
     */
    public static final String CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY = "closeAutoOrdDirtOrderSwitchKey";

    /**
     *直营分货单自动生效定时任务 key
     */
    public static final String AUTO_ORD_DIRT_ORDER_SWITCH = "autoOrdDirtOrderSwitch";

    /**
     * 组织关闭配货单中转采购任务key
     */
    public static final String CLOSE_AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH_KEY = "closeAutoTransferDirDeliveryOrderSwitchKey";

    /**
     * 组织配货单中转采购任务key
     */
    public static final String AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH = "ord:switch:autoTransferDirDeliveryOrderSwitch";

    /** 百分比 */
    public static final String PERCENTAGE = "100";


    public static final String DIS_CLOSE_CUT_ORDER_SWITCH_KEY = "disCloseCutOrderSwitchKey";

    /**
     * 天岁截单开关
     */
    public static final String DIS_TS_CLOSE_CUT_ORDER_SWITCH_KEY = "disTSCloseCutOrderSwitchKey";

    public static final String DIS_CUT_ORDER_SWITCH = "ord:switch:disCutOrder";

    public static final String DIR_CLOSE_CUT_ORDER_SWITCH_KEY = "dirCloseCutOrderSwitchKey";

    /**
     * 西安直营截单开关
     */
    public static final String DIR_SIA_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY = "dirSIADirectlyCloseCutOrderSwitchKey";

    /**
     * 西安加盟截单开关
     */
    public static final String DIS_SIA_FRANCHISE_CLOSE_CUT_ORDER_SWITCH_KEY = "dirSIAFranchiseCloseCutOrderSwitchKey";

    /**
     * 郑州直营截单开关
     */
    public static final String DIR_CGO_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY = "dirCGODirectlyCloseCutOrderSwitchKey";

    /**
     * 郑州加盟截单开关
     */
    public static final String DIR_CGO_FRANCHISE_CLOSE_CUT_ORDER_SWITCH_KEY = "dirCGOFranchiseCloseCutOrderSwitchKey";

    public static final String DIR_CUT_ORDER_SWITCH = "ord:switch:dirCutOrder";

    public static final String DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH_KEY = "disCloseSalvageDelivPondSwitchKey";

    public static final String DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH = "ord:switch:disCloseSalvageDelivPond";

    public static final String DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH_KEY = "dirCloseSalvageDelivPondSwitchKey";

    public static final String DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH = "ord:switch:dirCloseSalvageDelivPond";

    public static final String BUSINESS_ORDER_PAY_SERIAL_NO_KEY = "businessOrderPaySerialNo";

    public static final String DIS_CLOSE_WARNING_NO_AUTO_SALVAGE_SWITCH_KEY = "disCloseWarningNoAutoSalvageSwitchKey";

    public static final String DIS_WARNING_NO_AUTO_SALVAGE_SWITCH = "ord:switch:disWarningNoAutoSalvage";


    public static final String DIR_CLOSE_WARNING_NO_AUTO_SALVAGE_SWITCH_KEY = "dirCloseWarningNoAutoSalvageSwitchKey";

    public static final String DIR_WARNING_NO_AUTO_SALVAGE_SWITCH = "ord:switch:dirWarningNoAutoSalvage";

    public static final String WARNING_COUNT_KEY = "warningCountKey:";

    /**
     * 组织关闭直营订单任务任务key
     */
    public static final String CLOSE_AUTO_DIR_ORDER_SWITCH_KEY = "closeAutoDirOrderSwitchKey";

    /**
     * 组织关闭加盟订单任务任务key
     */
    public static final String CLOSE_AUTO_DIS_ORDER_SWITCH_KEY = "closeAutoDisOrderSwitchKey";

    /**
     * 组织是否打开关闭的开关任务
     */
    public static final String CLOSE_AUTO_ORDER_SWITCH_IS_OPEN = "OPEN";

    /**
     * 查询未定中转单定时器关闭
     */
    public static final String CLOSE_NO_TRANSFER_ORDER_SWITCH_KEY = "closeNoTransferOrderSwitchKey";

    /**
     * 查询特许加盟未定中转单开关key
     */
    public static final String FRANCHISE_NO_TRANSFER_ORDER_SWITCH = "ord:switch:franchiseNoTransferOrder";

    /**
     * 查询合作经营未定中转单开关key
     */
    public static final String DIRECTLY_NO_TRANSFER_ORDER_SWITCH = "ord:switch:directlyNoTransferOrder";

    /**
     * 关闭向物流发送加盟常温接单汇总信息
     */
    public static final String HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH_KEY = "handleFranchiseCutPurchaseOrderSummarySwitchKey";

    /**
     * 向物流发送加盟常温接单汇总信息开关key
     */
    public static final String HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH = "ord:switch:handleFranchiseCutPurchaseOrderSummary";

    /**
     * 关闭向物流发送直营常温接单汇总信息
     */
    public static final String HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH_KEY = "handleDirectlyCutPurchaseOrderSummarySwitchKey";

    /**
     * 向物流发送直营常温接单汇总信息开关key
     */
    public static final String HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH = "ord:switch:handleDirectlyCutPurchaseOrderSummary";

    /**
     * 关闭宝鸡物流发截单消息任务key
     */
    public static final String CLOSE_BJ_LOGISTICS_MESSAGE_SWITCH_KEY = "closeBJLogisticsMessageSwitchKey";

    /**
     * 宝鸡直营物流发截单消息开关key
     */
    public static final String BJ_LOGISTICS_MESSAGE_SWITCH = "ord:switch:bJLogisticsMessage";

    /**
     * 查询未正常转单定时器关闭
     */
    public static final String CLOSE_TO_REQUEST_ORDER_ERROR_SWITCH_KEY = "closeToRequestOrderErrorSwitchKey";

    /**
     * 查询未正常转单定时器关闭
     */
    public static final String TO_REQUEST_ORDER_ERROR_SWITCH = "ord:switch:toRequestOrderErrorSwitch";

    /**
     * 查询未正常拆单定时器关闭
     */
    public static final String CLOSE_TO_DELIVERY_ORDER_ERROR_SWITCH_KEY = "closeToDeliveryOrderErrorSwitchKey";

    /**
     * 查询未正常拆单定时器关闭
     */
    public static final String TO_DELIVERY_ORDER_ERROR_SWITCH = "ord:switch:toDeliveryOrderErrorSwitch";

    /**
     * 查询配货单未正常审核定时器关闭
     */
    public static final String CLOSE_AUDIT_DELIVERY_ERROR_SWITCH_KEY = "closeAuditDeliveryErrorSwitchKey";

    /**
     * 查询配货单未正常审核定时器关闭
     */
    public static final String AUDIT_DELIVERY_ERROR_SWITCH = "ord:switch:auditDeliveryErrorSwitch";

    /**
     * 查询配货单未正常审核定时器关闭
     */
    public static final String CLOSE_DELIVERY_AMOUNT_SIMILARITY_SWITCH_KEY = "closeDeliveryAmountSimilaritySwitchKey";

    /**
     * 查询配货单未正常审核定时器关闭
     */
    public static final String DELIVERY_AMOUNT_SIMILARITY_SWITCH = "ord:switch:deliveryAmountSimilaritySwitch";

    /** 任务定时任务超时锁  时间 */
    public final static long ASYNC_LOCK_TIME_OUT = 10 * 60 * 1000;

    /**
     * 任务循环最大次数
     */
    public final static long ASYNC_LOCK_REPEAT_TIME = 10;
    /**
     * 配货单超时未审核最大时间（单位：分钟）
     */
    public final static long DIR_DELIVERY_NO_AUDIT_MAX_OVER_TIME = 120;

    /**
     * 配销单超时未审核最大时间（单位：分钟）
     */
    public final static long DIS_DELIVERY_NO_AUDIT_MAX_OVER_TIME = 120;

    public static final String SEMICOLON = ";";

    public static final String ORD_DIR_DISTRIBUTION_ORDER_AUDIT = "ordDirDistributionOrderAudit";

    public static final String ORD_DIS_DISTRIBUTION_ORDER_AUDIT = "ordDisDistributionOrderAudit";

    public static final String ORD_DIR_FIRST_ORDER_AUDIT = "ordDirFirstOrderAudit";

    public static final String ORD_DIS_FIRST_ORDER_AUDIT = "ordDisFirstOrderAudit";


    /**
     * 批发中转推采购定时器关闭
     */
    public static final String CLOSE_PUSH_SHIPMENT_TO_PUR_SWITCH_KEY = "closePushShipmentToPurSwitchKey";

    /**
     * 批发中转推采购定时器关闭
     */
    public static final String PUSH_SHIPMENT_TO_PUR_SWITCH = "ord:switch:pushShipmentToPurSwitch";

    public static final String ORD_WHOLESALE_SHIPMENT_DTS_BACK_SHIPPED = "ordWholesaleShipmentDtsBackShipped";


    public static final String CLOSE_CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH_KEY = "closeCalculateGoodsStockSupplyRateSwitchKey";

    public static final String CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH = "ord:switch:calculateGoodsStockSupplyRate";

    public static final String STOCK_SUPPLY_RATE_TRUNCATION_DATE_TIME_KEY = "stockSupplyRateTruncationDateTimeKey";

    public static final String SUPPLY_RATE_STOCK_CODE_KEY = "SUPPLY_RATE_STOCK_CODE_KEY";

    /**
     * 组织关闭加盟点三三智能跑货订单任务任务key
     */
    public static final String CLOSE_AUTO_DIS_DSS_ORDER_SWITCH_KEY = "closeAutoDisDssOrderSwitchKey";

    /**
     * 加盟点三三上下限跑货定时器key
     */
    public static final String REPLENISHMENT_DIS_DSS_ORDER_SWITCH = "order:switch:disReplenishmentDssOrder";

    /**
     * 组织关闭直营点三三智能跑货订单任务任务key
     */
    public static final String CLOSE_AUTO_DIR_DSS_ORDER_SWITCH_KEY = "closeAutoDirDssOrderSwitchKey";

    /**
     * 直营点三三上下限跑货定时器key
     */
    public static final String REPLENISHMENT_DIR_DSS_ORDER_SWITCH = "order:switch:dirReplenishmentDssOrder";

    /**
     * 点三三门店已成功跑货key
     */
    public static final String DSS_STORE_ORDER_CREATED = "dssStoreOrderCreated:";

    /**
     * 直营订货单调配定时任务 开关
     */
    public static final String CLOSE_DIR_ORDER_ALLOCATION_POOL_SWITCH_KEY = "closeDirOrderAllocationPoolSwitchKey";

    /**
     *直营订货单调配定时任务 key
     */
    public static final String DIR_ORDER_ALLOCATION_POOL_SWITCH = "ord:switch:dirOrderAllocationPoolSwitch";

    /**
     * 直营订单调配创建订货单定时任务 开关
     */
    public static final String CLOSE_DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH_KEY = "closeDirAllocationPoolCreateOrderSwitchKey";

    /**
     *直直营订单调配创建订货单定时任务 key
     */
    public static final String DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH = "ord:switch:dirAllocationPoolCreateOrderSwitch";

    /**
     * 蜂行组织中转商品发采购任务执行时间
     */
    public static final String FX_EXECUTE_TIME = "10:00:00";

    /**
     * 蜂行直营截单开关
     */
    public static final String DIR_FX_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY = "dirFXDirectlyCloseCutOrderSwitchKey";

    /**
     * 调配池指定中分类计算订货数量计算公式不加1
     */
    public static final String DIR_ORD_ALLOCATION_POOL_SORT_KEY = "dirOrdAllocationPoolSortKey";

    public static final String ORD_WHOLESALE_RETURN_DTS_BACK_SHIPPED = "ordWholesaleReturnDtsBackShipped";

    public static final String DIS_CLOSE_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH_KEY = "disCloseUnfreezeBusinessOrderAndFreezeDeliverySwitchKey";

    public static final String DIS_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH = "ord:switch:disUnfreezeBusinessOrderAndFreezeDelivery";

    public static final String VERTICAL_BAR = "|";

    public static long HS_DIFF_DELAY_TIME = 10 * 60 * 1000;

    public static long MQ_DELAY_FAST_TIME = 10 * 20 * 1000;

    public static final String SHIPMENT_TRANSFER_IDS = "shipmentTransferIds:";
}

