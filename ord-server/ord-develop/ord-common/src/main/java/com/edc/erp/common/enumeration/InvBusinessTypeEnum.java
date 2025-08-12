package com.edc.erp.common.enumeration;

/**
 * 调用库存业务类型枚举
 * @author weichao
 */
public enum InvBusinessTypeEnum {
    /**
     * 仓储采购收货
     */
    WAREHOUSE_COLLECT("warehouseCollect", "仓储采购收货"),
    /**
     * 仓储采购退货
     */
    WAREHOUSE_RETREAT("warehouseRetreat", "仓储采购退货"),
    /**
     * 门店采购收货
     */
    STORE_PURCHASE_IN("storePurchaseIn", "门店采购收货"),
    /**
     * 门店采购退货
     */
    STORE_PURCHASE_OUT("storePurchaseOut", "门店采购退货"),
    /**
     * 统配出货
     */
    ALLOCATION_OUT("allocationOut", "统配出货"),
    /**
     * 统配差异-正
     */
    ALLOCATION_POOR_JUST("allocationPoorJust", "统配差异-正"),
    /**
     * 统配差异-负
     */
    ALLOCATION_POOR_LOSS("allocationPoorLoss", "统配差异-负"),

    /**
     * 统配退货
     */
    ALLOCATION_RETREAT("allocationRetreat", "统配退货"),
    /**
     * 配销出货
     */
    DISTRIBUTIVE_OUT("distributiveOut", "配销出货"),
    /**
     * 配销差异-正
     */
    DISTRIBUTIVE_POOR_JUST("distributivePoorJust", "配销差异-正"),
    /**
     * 配销差异-负
     */
    DISTRIBUTIVE_POOR_LOSS("distributivePoorLoss", "配销差异-负"),

    /**
     * 配销退货
     */
    DISTRIBUTIVE_RETREAT("distributiveRetreat", "配销退货"),
    /**
     * 批发出货
     */
    WHOLESALE_OUT("wholesaleOut", "批发出货"),
    /**
     * 批发退货
     */
    WHOLESALE_RETREAT("wholesaleRetreat", "批发退货"),
    /**
     * 门店销售
     */
    STORE_SALE("storeSale", "门店销售"),
    /**
     * 门店销售退货
     */
    STORE_SALE_RETREAT("storeSaleRetreat", "门店销售退货"),
    /**
     * 门店外卖
     */
    STORE_TAKE_OUT("storeTakeOut", "门店外卖"),
    /**
     * 门店外卖退货
     */
    STORE_TAKE_OUT_RETREAT("storeTakeOutRetreat", "门店外卖退货"),
    /**
     * 仓储盘点
     */
    WAREHOUSE_INVENTORY("warehouseInventory", "仓储盘点"),
    /**
     * 仓储库存调整
     */
    WAREHOUSE_STOCK("warehouseStock", "仓储库存调整"),
    /**
     * 仓储移库
     */
    WAREHOUSE_MOVE_STOCK("warehouseMoveStock", "仓储移库"),
    /**
     * 仓储加工
     */
    WAREHOUSE_MACHINING("warehouseMachining", "仓储加工"),
    /**
     * 门店自主盘点
     */
    STORE_INVENTORY_SC("storeInventorySc", "门店自主盘点"),
    /**
     * 门店盘点
     */
    STORE_INVENTORY("storeInventory", "门店盘点"),
    /**
     * 门店盘点
     */
    STORE_INVENTORY_LIST("storeInventoryList", "盘点目录"),
    /**
     * 门店盘点
     */
    INVENTORY_IN_LIST("InventoryInList", "盘入单"),
    /**
     * 门店盘点
     */
    STOCK_ADJUSTMENT("StockAdjustment", "库存快照调整单"),
    /**
     * 门店库存调整
     */
    STORE_STOCK("storeStock", "门店库存调整"),
    /**
     * 门店报损
     */
    STORE_FAULTY("storeFaulty", "门店报损"),
    /**
     * 门店领用
     */
    STORE_COLLECT("storeCollect", "门店领用"),
    /**
     * 门店调拨
     */
    STORE_ALLOCATION("storeAllocation", "门店调拨"),
    /**
     * 门店加工-计划性
     */
    STORE_MACHINING_PLAN("storeMachiningPlan", "门店加工-计划性"),
    /**
     * 门店加工-非计划性
     */
    STORE_MACHINING_UNPLAN("storeMachiningUNPlan", "门店加工-非计划性"),
    /**
     * 仓储调拨
     */
    WAREHOUSE_ALLOCATION("warehouseAllocation", "仓储调拨"),
    ;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    InvBusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (InvBusinessTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
