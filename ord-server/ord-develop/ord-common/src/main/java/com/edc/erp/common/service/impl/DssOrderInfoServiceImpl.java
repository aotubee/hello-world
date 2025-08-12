package com.edc.erp.common.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OtherReplenishmentConfig;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.mapper.OtherReplenishmentConfigMapper;
import com.edc.erp.common.mapper.dss.DssOrderInfoMapper;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.DssOrderInfoOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import com.edc.erp.common.rpc.StoreCenterClient;
import com.edc.erp.common.service.DssOrderInfoService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName DssOrderInfoServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/18 11:02
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DssOrderInfoServiceImpl implements DssOrderInfoService {

    private final DssOrderInfoMapper dssOrderInfoMapper;

    private final OtherReplenishmentConfigMapper otherReplenishmentConfigMapper;

    private final StoreCenterClient storeCenterClient;

    private final OrderGoodsServer orderGoodsServer;

    private final RedisService redisService;

    @Override
    public List<DssOrderInfoOut> findDssOrderInfoList(List<String> storeCodeList, String beginTime, String endTime) {
        List<DssOrderInfoOut> dssOrderInfoList = dssOrderInfoMapper.findDssOrderInfoList(storeCodeList, beginTime, endTime);
        return dssOrderInfoList;
    }

    /**
     * @Description: 处理点三三智能跑货商品（校验和封装下单数据）
     * @Author: ZhangYao
     * @Date: 2024/6/18 16:40
     * @param bizOrgCode:
     * @return: java.util.Map<java.lang.String, java.util.List < com.edc.erp.common.model.out.DssOrderInfoOut>>
     **/
    @Override
    public Map<String, List<DssOrderInfoOut>> handleDssReplenishment(String bizOrgCode, String storeProperty) {
        List<OtherReplenishmentConfig> otherReplenishmentConfigList = otherReplenishmentConfigMapper.findNeedOtherReplenishmentStoreList(bizOrgCode, storeProperty);
        if (CollectionUtils.isEmpty(otherReplenishmentConfigList)) {
            log.info("加盟------业务组织{}没有要智能跑货的配置", bizOrgCode);
            return null;
        }
        Map<String, List<OtherReplenishmentConfig>> otherReplenishmentConfigMap = otherReplenishmentConfigList.stream()
                .collect(Collectors.groupingBy(otherReplenishmentConfig -> otherReplenishmentConfig.getStoreCode()));
        List<String> storeCodeList = otherReplenishmentConfigMap.keySet().stream().distinct().collect(Collectors.toList());
        LocalDateTime beginTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime endTime = LocalDateTime.now();
        List<DssOrderInfoOut> dssOrderInfoList = this.findDssOrderInfoList(storeCodeList, DateUtils.format(beginTime), DateUtils.format(endTime));
        log.info("智能补货门店数量{}---明细数量{}", storeCodeList.size(), dssOrderInfoList.size());
        if (CollectionUtils.isEmpty(dssOrderInfoList)) {
            log.info("{}---至---{},点三三业务组织{}下未发现跑货订单信息", DateUtils.format(beginTime), DateUtils.format(endTime), bizOrgCode);
            return null;
        }
        // 按门店代码分组点三三商品
        Map<String, List<DssOrderInfoOut>> dssStoreOrderMap = dssOrderInfoList.stream().collect(Collectors.groupingBy(dssOrderInfoOut -> dssOrderInfoOut.getSFdbh()));
        Map<String, List<DssOrderInfoOut>> storeDssGoodsMap = new LinkedHashMap<>();
        storeCodeList.forEach(storeCode -> {
            try {
                String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(LocalDate.now()) + SystemConstant.COLON + storeCode;
                if (redisService.hasKey(key)) {
                    log.info("智能补货门店{}今天已成功创建订单", storeCode);
                    return;
                }
                List<DssOrderInfoOut> storeDssOrderInfoOutList = dssStoreOrderMap.get(storeCode);
                // 点三三订单信息
                if (CollectionUtils.isEmpty(storeDssOrderInfoOutList)) {
                    log.info("{}---至---{},点三三业务组织{}下智能补货门店{}未发现跑货订单信息", DateUtils.format(beginTime), DateUtils.format(endTime), bizOrgCode, storeCode);
                    return;
                }
                log.info("智能补货门店{}-----点三三推明细共{}条", storeCode, storeDssOrderInfoOutList.size());
                List<OtherReplenishmentConfig> storeOtherReplenishmentConfigList = otherReplenishmentConfigMap.get(storeCode);
//                if (CollectionUtils.isEmpty(storeOtherReplenishmentConfigList)) {
//                    log.info("门店{}暂无智能跑货配置", storeCode);
//                    return;
//                }
                this.checkAndInitStoreDssOrderGoods(bizOrgCode, storeCode, storeDssOrderInfoOutList, beginTime, endTime, storeDssGoodsMap, storeOtherReplenishmentConfigList, storeProperty);
            } catch (Exception e) {
                log.error("智能补货门店{}校验点三三商品异常", storeCode, e);
            }
        });
        return storeDssGoodsMap;
    }


    @Override
    public Map<String, List<DssOrderInfoOut>> subMap(Map<String, List<DssOrderInfoOut>> map, int start, int end) {
        Map<String, List<DssOrderInfoOut>> result = new LinkedHashMap<>();
        Iterator<Map.Entry<String, List<DssOrderInfoOut>>> iterator = map.entrySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Map.Entry<String, List<DssOrderInfoOut>> entry = iterator.next();
            if (i >= start && i < end) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private void checkAndInitStoreDssOrderGoods(String bizOrgCode, String storeCode, List<DssOrderInfoOut> storeDssOrderInfoOutList,
                                                LocalDateTime beginTime, LocalDateTime endTime, Map<String, List<DssOrderInfoOut>> storeDssGoodsMap,
                                                List<OtherReplenishmentConfig> storeOtherReplenishmentConfigList, String storeProperty) {

        StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
        storeStatusInfo.setBizOrgCode(bizOrgCode);
        storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.UP_LOW_DOWN.getType());
        storeStatusInfo.setStoreProperty(storeProperty);
        storeStatusInfo.setStoreCode(storeCode);
        Response<StoreInfo> response = storeCenterClient.getStatusStoreInfoByAuthOrg(storeStatusInfo);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("点三三自动补货，智能补货门店" + storeCode + response.getMessage());
            return;
        }
        Map<String, OtherReplenishmentConfig> storeReplenishmentConfigMap = storeOtherReplenishmentConfigList.stream()
                .collect(Collectors.toMap(OtherReplenishmentConfig::getGoodsCode, Function.identity()));
        //商品取交集
        List<DssOrderInfoOut> equalGoodsCodeList = storeDssOrderInfoOutList.stream().filter(dssGoods -> this.firstFilterDss(dssGoods, storeReplenishmentConfigMap)).collect(Collectors.toList());
//        // 找出配置与点三三实际返回订单明细商品差集
//        List<OtherReplenishmentConfig> diffList = storeOtherReplenishmentConfigList.stream()
//                .filter(config -> storeDssOrderInfoOutList.stream().noneMatch(dssOrderInfoOut -> config.getGoodsCode().equals(dssOrderInfoOut.getSSpbh()))).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(diffList)) {
//            String goodsCodeStr = diffList.stream().map(OtherReplenishmentConfig::getGoodsCode).collect(Collectors.joining(SystemConstant.COMMA));
//            log.info("{}---至---{},点三三业务组织{}下门店{}跑货商品与配置商品代码{}不符", DateUtils.format(beginTime), DateUtils.format(endTime), bizOrgCode, storeCode, goodsCodeStr);
//            return;
//        }
        if (CollectionUtils.isEmpty(equalGoodsCodeList)) {
            log.info("{}---至---{},点三三业务组织{}下智能补货门店{}跑货商品与配置商品代码没有匹配的数据", DateUtils.format(beginTime), DateUtils.format(endTime), bizOrgCode, storeCode);
            return;
        }
        List<String> goodsCodeList = equalGoodsCodeList.stream().map(DssOrderInfoOut::getSSpbh).collect(Collectors.toList());
        OrdQueryGoodsIn ordQueryGoodsIn = new OrdQueryGoodsIn();
        ordQueryGoodsIn.setBizOrgCode(bizOrgCode);
        ordQueryGoodsIn.setGoodsCodes(goodsCodeList);
        ordQueryGoodsIn.setStoreCode(storeCode);
        ordQueryGoodsIn.setBusinessType(BusinessTypeColumnEnum.UP_LOW_DOWN.getType());
//        List<CheckUpperLowerGoodsOut> checkGoodsList = orderGoodsServer.findGoodsInfoForCheckUppLower(queryCheckUpperLowerGoodsIn);
        List<OrderGoodsOut> orderGoodsOutList = orderGoodsServer.findForUpDownLimit(ordQueryGoodsIn);
        if (CollectionUtils.isEmpty(orderGoodsOutList)) {
            log.info("智能补货门店{}校验组织商品全部为空,请检查组织商品规格配置", storeCode);
            return;
        }
        Map<String, OrderGoodsOut> checkGoodsMap = orderGoodsOutList.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
        equalGoodsCodeList.forEach(dssOrderInfoOut -> {
            OrderGoodsOut checkUpperLowerGoodsOut = checkGoodsMap.get(dssOrderInfoOut.getSSpbh());
            if (Objects.isNull(checkUpperLowerGoodsOut)) {
                log.info("智能补货门店{}校验组织商品{}为空，请检查组织商品是否配置跑货", storeCode, dssOrderInfoOut.getSSpbh());
                return;
            }
            StandardSpecTransInfoOut standardSpecTransInfoOut = checkUpperLowerGoodsOut.getDistributionSpecification();
            if (Objects.isNull(standardSpecTransInfoOut)) {
                log.info("智能补货门店{}校验组织商品{}配送规格为空", storeCode, dssOrderInfoOut.getSSpbh());
                return;
            }
            if (Objects.isNull(checkUpperLowerGoodsOut.getDistributionUnitPrice())) {
                log.info("智能补货门店{}校验组织商品{}配送价为空", storeCode, dssOrderInfoOut.getSSpbh());
                return;
            }
            boolean isCanUpperLowerFlag = NumberUtil.INTEGER_ONE.equals(checkUpperLowerGoodsOut.getIsUpDownLimit()) ? true : false;
            if (!isCanUpperLowerFlag) {
                log.info("智能补货门店{}校验组织商品{}不允许跑货", storeCode, dssOrderInfoOut.getSSpbh());
                return;
            }
            boolean isCanUpDownLimitLogcFlag = NumberUtil.INTEGER_ONE.equals(checkUpperLowerGoodsOut.getIsUpDownLimitLogc()) ? true : false;
            if (!isCanUpDownLimitLogcFlag) {
                log.info("智能补货门店{}校验仓位{}不允许跑货", storeCode, checkUpperLowerGoodsOut.getStockCode());
                return;
            }
//            if (Objects.isNull(dssOrderInfoOut.getNsl()) || dssOrderInfoOut.getNsl().compareTo(BigDecimal.ZERO) < NumberUtil.INTEGER_ONE) {
//                log.info("门店{}智能补货校验组织商品{}数量为{}", storeCode, dssOrderInfoOut.getSSpbh(), dssOrderInfoOut.getNsl());
//                return;
//            }
            // 封装合法门店商品数据
            List<DssOrderInfoOut> dssGoodsList = storeDssGoodsMap.get(storeCode);
            if (CollectionUtils.isEmpty(dssGoodsList)) {
                dssGoodsList = Lists.newArrayList();
            }
            dssOrderInfoOut.setNsl(dssOrderInfoOut.getNsl().divide(new BigDecimal(standardSpecTransInfoOut.getQpc()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN));
            dssGoodsList.add(dssOrderInfoOut);
            storeDssGoodsMap.put(storeCode, dssGoodsList);
        });
    }

    private boolean firstFilterDss(DssOrderInfoOut dssGoods, Map<String, OtherReplenishmentConfig> storeReplenishmentConfigMap) {
        return storeReplenishmentConfigMap.containsKey(dssGoods.getSSpbh()) && Objects.nonNull(dssGoods.getNsl()) && dssGoods.getNsl().compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO;
    }
}
