package com.edc.erp.enumeration;

import com.edc.erp.common.enumeration.OrgRelationEnum;
import com.edc.erp.model.out.BoxOut;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动类型枚举类，包含标签
 * @author fxw
 */
public enum PaymentChannelTypeEnum {

    /**
     * 线上APP支付
     */
    ONLINE_APP("0010001", "线上APP支付"),
    /**
     * 线上WEB支付
     */
    ONLINE_WEB("0010002", "线上WEB支付"),
    /**
     * 线下主扫
     */
    OFFLINE_GIVING("0010003", "线下主扫"),
    /**
     * 线下被扫
     */
    OFFLINE_PASSIVE_SCAN("0010004", "线下被扫");

    private final String code;
    private final String tagName;

    PaymentChannelTypeEnum(String code, String tagName) {
        this.code = code;
        this.tagName = tagName;
    }

    public String getCode() {
        return code;
    }

    public static String getCode(PaymentChannelTypeEnum activityTypeEnum) {
        return activityTypeEnum.code;
    }

    public static String getTagName(PaymentChannelTypeEnum activityTypeEnum) {
        return activityTypeEnum.tagName;
    }

    public static String getTagNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (PaymentChannelTypeEnum activityTypeEnum : PaymentChannelTypeEnum.values()) {
            if (code.equals(activityTypeEnum.code)) {
                return activityTypeEnum.tagName;
            }
        }
        return "";
    }
    public enum PaymentPlatformTypeEnum {
        /**
         * 支付宝
         */
        ALI_PAY("0007001", "支付宝"),
        /**
         * 微信
         */
        WX_PAY("0007002", "微信"),
        /**
         *银联
         */
        CHINA_PAY("0007003", "银联"),
        /**
         *小浦支付
         */
        SPD_BANK_PAY("0007004", "小浦支付"),
        /**
         *余额支付
         */
        BALANCE_PAY("0000000", "余额支付"),
        /**
         *直选银盛支付宝
         */
        PLATFORM_YSE_ALIPAY("0007009", "直选银盛支付宝"),
        /**
         *直选银盛微信
         */
        PLATFORM_YSE_WECHAT("0007010", "直选银盛微信"),
        /**
         *宝鸡银盛支付宝
         */
        PLATFORM_YSE_BAOJI_ALIPAY("0007007", "宝鸡银盛支付宝"),
        /**
         *宝鸡银盛微信
         */
        PLATFORM_YSE_BAOJI_WECHAT("0007008", "宝鸡银盛微信"),
        /**
         *天岁富友支付宝
         */
        PLATFORM_FUIOU_ALIPAY("0007005", "天岁富友支付宝"),
        /**
         *天岁富友微信
         */
        PLATFORM_FUIOU_WECHAT("0007006", "天岁富友微信"),
        ;

        private final String code;
        private final String tagName;

        PaymentPlatformTypeEnum(String code, String tagName) {
            this.code = code;
            this.tagName = tagName;
        }

        public String getCode() {
            return code;
        }

        public static String getCode(PaymentChannelTypeEnum activityTypeEnum) {
            return activityTypeEnum.code;
        }

        public static String getTagName(PaymentChannelTypeEnum activityTypeEnum) {
            return activityTypeEnum.tagName;
        }

        public static String getPaymentPlatformTypeEnumTagNameByCode(String code) {
            if (StringUtils.isBlank(code)) {
                return null;
            }
            for (PaymentPlatformTypeEnum paymentPlatformTypeEnum : PaymentPlatformTypeEnum.values()) {
                if (code.equals(paymentPlatformTypeEnum.code)) {
                    return paymentPlatformTypeEnum.tagName;
                }
            }
            return "";
        }

//        public static List<BoxOut> findPlatformByOrg(String orgCode){
//            List<BoxOut> list = new ArrayList<>();
//            BoxOut balanceOut = new BoxOut(BALANCE_PAY.code, BALANCE_PAY.tagName);
//            list.add(balanceOut);
//            if (StringUtils.isBlank(orgCode)) {
//                return list;
//            }
//            if (OrgRelationEnum.getMytCode(OrgRelationEnum.XIAN_MYT).equals(orgCode)
//            || OrgRelationEnum.getMytCode(OrgRelationEnum.TIANSUI_MYT).equals(orgCode)) {
//                BoxOut spdOut = new BoxOut(SPD_BANK_PAY.code, SPD_BANK_PAY.tagName);
//                list.add(spdOut);
//                BoxOut yesAliOut = new BoxOut(PLATFORM_FUIOU_ALIPAY.code, PLATFORM_FUIOU_ALIPAY.tagName);
//                list.add(yesAliOut);
//                BoxOut yesWechatOut = new BoxOut(PLATFORM_FUIOU_WECHAT.code, PLATFORM_FUIOU_WECHAT.tagName);
//                list.add(yesWechatOut);
//            }
//            if (OrgRelationEnum.getMytCode(OrgRelationEnum.BAOJI_MYT).equals(orgCode)) {
//                BoxOut yesAliOut = new BoxOut(PLATFORM_YSE_BAOJI_ALIPAY.code, PLATFORM_YSE_BAOJI_ALIPAY.tagName);
//                list.add(yesAliOut);
//                BoxOut yesWechatOut = new BoxOut(PLATFORM_YSE_BAOJI_WECHAT.code, PLATFORM_YSE_BAOJI_WECHAT.tagName);
//                list.add(yesWechatOut);
//            }
//            return list;
//        }
    }

    public enum PaymentTradeTypeEnum {
        /**
         * JSAPI支付或小程序支付
         */
        JS_API("JSAPI", "JSAPI支付或小程序支付"),
        /**
         * Native支付
         */
        NATIVE("NATIVE", "Native支付"),
        /**
         * app支付
         */
        APP("APP", "app支付"),
        /*
         *H5支付
         */
        M_WEB("0007004", "H5支付");

        private final String code;
        private final String tagName;

        PaymentTradeTypeEnum(String code, String tagName) {
            this.code = code;
            this.tagName = tagName;
        }

        public String getCode() {
            return code;
        }

        public static String getCode(PaymentChannelTypeEnum activityTypeEnum) {
            return activityTypeEnum.code;
        }

        public static String getTagName(PaymentChannelTypeEnum activityTypeEnum) {
            return activityTypeEnum.tagName;
        }

        public static String getTagNameByCode(String code) {
            if (StringUtils.isBlank(code)) {
                return null;
            }
            for (PaymentChannelTypeEnum activityTypeEnum : PaymentChannelTypeEnum.values()) {
                if (code.equals(activityTypeEnum.code)) {
                    return activityTypeEnum.tagName;
                }
            }
            return "";
        }
    }
}
