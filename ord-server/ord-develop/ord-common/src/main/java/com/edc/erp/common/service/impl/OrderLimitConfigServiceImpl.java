package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.OrderLimitConfig;
import com.edc.erp.common.mapper.OrderLimitConfigMapper;
import com.edc.erp.common.service.OrderLimitConfigService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lishaobo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderLimitConfigServiceImpl extends BaseServiceImpl<OrderLimitConfig> implements OrderLimitConfigService {

    private final OrderLimitConfigMapper orderLimitConfigMapper;

    @Override
    public Map<String, BigDecimal> findGoodsLimitByStoreCode(String storeCode, String bizOrgCode) {
        List<OrderLimitConfig> list = orderLimitConfigMapper.select(OrderLimitConfig.builder()
                .storeCode(storeCode).bizOrgCode(bizOrgCode).isDelete(ModelConst.DELETE.NO).build());
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(NumberUtil.INTEGER_ZERO);
        }
        return list.stream().collect(Collectors.toMap(OrderLimitConfig::getGoodsCode, OrderLimitConfig::getOrderUpperLimit));
    }
}
