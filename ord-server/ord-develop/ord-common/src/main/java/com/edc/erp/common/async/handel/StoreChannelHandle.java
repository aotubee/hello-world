package com.edc.erp.common.async.handel;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.plugins.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName ChannelHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/1/4 9:11
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class StoreChannelHandle {

    private final StoreCenterService storeCenterService;

    /**
     * @Description: 通过门店获取对应渠道组织的bizOrgCode
     * @Author: ZhangYao
     * @Date: 2024/1/4 9:13
     * @param storeCode:
     * @return: java.lang.String
     **/
    public String getStoreChannelBizOrgCode(String storeCode, String bizOrgCode) {
        StoreInfo storeInfo = storeCenterService.getStoreInfoByStoreCode(storeCode, bizOrgCode);
        if (Objects.isNull(storeInfo)) {
            return null;
        }
        return storeInfo.getBizOrgCode();
    }

    public Map<String, String> findStoreChannelBizOrgCode(AsyncDeliveryImportInfoIn asyncDeliveryImportInfoIn) {
        List<StoreInfo> storeInfoList = storeCenterService.findStoreInfoByStoreCodeList(asyncDeliveryImportInfoIn);
        if (CollectionUtils.isEmpty(storeInfoList)) {
            return new HashMap<>();
        }
        return storeInfoList.stream().collect(Collectors.toMap(StoreInfo::getErpStoreCode, StoreInfo::getBizOrgCode));
    }

    public void checkStockCodeListISAuth(List<String> businessStockCodeList, Map<String, StockInfoOut> authOrgStockMap) {
        List<String> noAuthStockCodeList = businessStockCodeList.stream().filter(stockCode -> !authOrgStockMap.containsKey(stockCode)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noAuthStockCodeList)) {
            throw new BusinessException(String.join(SystemConstant.COMMA, noAuthStockCodeList) + "未被允许操作");
        }
    }
}
