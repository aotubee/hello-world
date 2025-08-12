package com.edc.erp.common.service;

import com.edc.erp.common.model.out.DssOrderInfoOut;

import java.util.List;
import java.util.Map;

public interface DssOrderInfoService {
    List<DssOrderInfoOut> findDssOrderInfoList(List<String> storeCodeList, String beginTime, String endTime);

    Map<String, List<DssOrderInfoOut>> handleDssReplenishment(String bizOrgCode, String storeProperty);

    Map<String, List<DssOrderInfoOut>> subMap(Map<String, List<DssOrderInfoOut>> map, int start, int end);
}
