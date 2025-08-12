package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-08-11 14:11
 */
@Data
public class LogisticsMessageOrderQuantityIn extends BaseEntity {

    /**
     *物流统计类型， 1:散件，2:整件，3：高值
     */
    private Integer statisticalType;

    private String bizOrgCode;

    private List<Integer> orderIdList;
}
