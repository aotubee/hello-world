package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-02-25 16:44
 */
@Data
public class CutPurchaseOrderSummaryIn extends BaseEntity {

    /**
     * 门店属性
     */
    private String storeProperty;

    /**
     * 温层最小起订额
     */
    private BigDecimal minimumAmount;

    /**
     * 接单时间
     */
    private String acceptTime;

    /**
     * 温层
     */
    private String orderType;

    /**
     * 钉钉机器人发送地址
     */
    private String url;

    /**
     * 钉钉用户手机号
     */
    private List<String> mobileList;

    /**
     * 物流统计类型，1:散件，2:整件，3：高值
     */
    private Integer statisticalType;

    /**
     * 组织代码
     */
    private String bizOrgCode;

    private List<String> storeCodeList;
}
