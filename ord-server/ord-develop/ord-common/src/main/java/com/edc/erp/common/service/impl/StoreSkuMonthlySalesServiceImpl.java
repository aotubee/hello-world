package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.StoreSkuMonthlySales;
import com.edc.erp.common.mapper.StoreSkuMonthlySalesMapper;
import com.edc.erp.common.service.StoreSkuMonthlySalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreSkuMonthlySalesServiceImpl implements StoreSkuMonthlySalesService {

    private final StoreSkuMonthlySalesMapper storeSkuMonthlySalesMapper;

    @Override
    public Map<String, BigDecimal> getSkuAverageDailySalesDaysListByStoreCode(List<String> skuList, String storeCode, String bizOrgCode) {
        Map<String, BigDecimal> map = new HashMap<>();
        List<StoreSkuMonthlySales> storeSkuMonthlySalesList = storeSkuMonthlySalesMapper.findSkuAverageDailySalesDaysListByStoreCode(skuList, storeCode, bizOrgCode);
        if (CollectionUtils.isNotEmpty(storeSkuMonthlySalesList)) {
            map = storeSkuMonthlySalesList.stream().collect(Collectors.toMap(StoreSkuMonthlySales::getSkuCode, StoreSkuMonthlySales::getMonthlySales));
        }
        return map;
    }

}
