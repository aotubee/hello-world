package com.edc.erp.directly.enumeration;

/**
 * 单据生命周期流程详情配置枚举
 *
 * @author wanglidong
 * @since 2022/11/16 17:07
 */
public enum OrderCycleProcessConfigItemCodeEnum {

    /**
     * 单据生命周期流程详情配置
     */
    MANUAL("I000001", "手工订货"),
    DISTRIBUTION("I000002", "人工分货"),
    UP_AND_DOWN("I000003", "系统补货"),
    ORDER_SINGLE("I000004", "单笔"),
    ORDER_CUMULATIVE("I000005", "周期内累加"),
    NO_CHECK_ACTIVITY("I000006", "促销不校验"),
    CHECK_ACTIVITY("I000007", "校验促销"),
    ORDER_SUBMIT("I000008", "订货单已提交"),
    ORDER_PAID("I000009", "订货单已付款"),
    TO_REQUEST_ORDER("I000010", "订货单已转单"),
    IMMEDIATELY_CREATE_REQUEST_ORDER("I000011", "立即转要货单"),
    WAIT_CREATE_REQUEST_ORDER("I000012", "到截单时间转要货单"),
    XI_AN_WECHAT_PAY("I000013", "西安每一天-直选银盛微信"),
    XI_AN_AILI_PAY("I000014", "西安每一天-直选银盛支付宝"),
    XI_AN_PD_PAY("I000015", "西安每一天-小浦支付"),
    BAO_JI_WECHAT_PAY("I000016", "宝鸡每一天-银盛微信"),
    BAO_JI_AILI_PAY("I000017", "宝鸡每一天-银盛支付宝"),
    BALANCE_PAY("I000018", "纯余额支付"),
    FREEZE_BALANCE("I000019", "仅冻结"),
    DEDUCT_MONEY("I000020", "扣除资金"),
    ACCUMULATION("I000021", "累加"),
    COVER("I000022", "覆盖"),
    DELIVERY_SPLIT_POSITION("I000023", "仓位"),
    DELIVERY_SPLIT_DISTRIBUTION_TYPE("I000024", "配送方式"),
    AUTO_RECEIVE("I000025", "自动收货"),
    MANUAL_RECEIVE("I000026", "非自动收货"),
    TAKE_BIG_VALUE("I000027", "取大值"),
    XI_AN_FUIOU_AILI_PAY("I000028", "西安每一天-富友支付宝"),
    XI_AN_FUIOU_WECHAT_PAY("I000029", "西安每一天-富友微信"),
    GOODS_TYPE("I000030", "品类属性"),
    ONE("I000031", "转单优先级1"),
    TWO("I000032", "转单优先级2"),
    THREE("I000033", "转单优先级3"),
    EVERYDAY_PAY("I000034", "每一天支付"),
    ;


    private String code;
    private String value;

    OrderCycleProcessConfigItemCodeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByCode(String code) {
        for (OrderCycleProcessConfigItemCodeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getValue();
            }
        }
        return null;
    }

}