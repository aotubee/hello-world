package com.edc.erp.common.enumeration.warning;

/**
 * 上下限跑货清单预警类型枚举类型枚举类
 * @author w
 */
public enum UpLowerLimitListWarningTypeEnum {

    UP_LOWER_LIMIT_AMOUNT("upLowerLimitAmount", "{0}门店系统补货订单金额({1})超近期{2}笔订单平均金额的一倍"),
    UP_LOWER_LIMIT_SKU_SIMILARITY("upLowerLimitSkuSimilarity", "门店{0}跑货清单：{1}与{2}相似度达{3}%"),
//    NEW_UP_LOWER_LIMIT_AMOUNT("newUpLowerLimitAmount", "{0}上下限跑货订单金额过大预警：系统补货订单金额({1})超近期30笔订单平均金额的一倍，请关注！"),
    NEW_UP_LOWER_LIMIT_AMOUNT("newUpLowerLimitAmount", "### {0}\n" +
            "\n" +
            "#### 上下限跑货订单金额过大预警：系统补货订单金额({1})超近期30笔订单平均金额的一倍，请关注！"),
    NEW_UP_LOWER_LIMIT_SKU_SIMILARITY("newUpLowerLimitSkuSimilarity", "### {0}\n" +
            "\n" +
            "#### 上下限跑货单与上一周期跑货单相似度过高预警：系统补货订单：{1}与{2}相似度达{3}%，请关注！"),
//    NEW_UP_LOWER_LIMIT_SKU_SIMILARITY("newUpLowerLimitSkuSimilarity", "门店{0}跑货清单：{1}与{2}相似度达{3}%"),
    REQUEST_HD_STORE_SKU_INVENTORY_ERROR("requestHdStoreSkuInventory", "门店{0}获取海鼎实时库存异常"),
    REQUEST_IN_TRANSIT_STORE_SKU_QUANTITY_ERROR("requestInTransitStoreSkuQuantity", "门店{0}获取海鼎在单量异常"),
    UP_LOWER_LIMIT_NOT_RUNNING_CARGO("upLowerLimitNotRunningCargo", "### 【{0}-门店跑货异常预警】\n" +
            "\n" +
            "#### {1}\n" +
            "#### 跑货异常，请关注！\n" +
            "#### {2}"),
    UP_LOWER_LIMIT_FILTER_GOODS_NOTICE("upLowerLimitFilterGoodsNotice", "### 【{0}-门店跑货过滤无效商品通知】\n" +
            "\n" +
            "#### {1}\n" +
            "#### 跑货过滤商品，请关注！\n" +
            "#### {2}"),
    ;

    private final String type;
    private final String errorMessage;

    UpLowerLimitListWarningTypeEnum(String type, String errorMessage) {
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public String getType() {
        return type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static String getCode(UpLowerLimitListWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.type;
    }

    public static String getName(UpLowerLimitListWarningTypeEnum activityTypeEnum) {
        return activityTypeEnum.errorMessage;
    }

    public static String getNameByType(String type) {
        for (UpLowerLimitListWarningTypeEnum upLowerLimitListWarningTypeEnum : values()) {
            if (upLowerLimitListWarningTypeEnum.getType().equals(type)) {
                return upLowerLimitListWarningTypeEnum.getErrorMessage();
            }
        }
        return "";
    }
}
