package com.edc.erp.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StoreSkuMonthlySalesService {

    Map<String, BigDecimal> getSkuAverageDailySalesDaysListByStoreCode(List<String> skuList, String storeCode, String bizOrgCode);
}
