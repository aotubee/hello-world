package com.edc.erp.disdeliveryorder.handle;

import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.in.QueryPurchaseIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.QueryPurchaseOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.model.excel.OrdDisDeliveryImportErrorResult;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disdeliveryorder.model.out.DisDeliveryImportCheckOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @ClassName DisAsyncImportDeliveryHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/21 18:02
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DisDeliveryImportHandle {

    private final OrdDisDeliveryService ordDisDeliveryService;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final StoreCenterService storeCenterService;

    private final OrderGoodsServer orderGoodsServer;

    private final StockServer stockServer;

    private final AsyncExportHandle asyncExportHandle;

    private final RedisService redisService;

    private final StoreChannelHandle storeChannelHandle;

    private final PurchaseOrderClient purchaseOrderClient;

    @Async
    public void handleDeliveryListAsyncImport(List<ImportDisDeliveryOrder> importDisDeliveryOrderList, String loginUsername, String loginBizOrgCode, String type, String key) {
        try {
            DisDeliveryImportCheckOut disDeliveryImportCheckOut = this.checkImportSaveDeliveryOrderDetail(importDisDeliveryOrderList, loginBizOrgCode);
            List<ImportDisDeliveryOrder> deliveryOrderList = disDeliveryImportCheckOut.getDeliveryOrderList();
            if (CollectionUtils.isNotEmpty(deliveryOrderList)) {
                List<OrdDisDeliveryIn> ordDisDeliveryInList = ordDisDeliveryService.initDeliveryOrderAndDetailForAsyncImport(deliveryOrderList,
                        disDeliveryImportCheckOut.getStoreInfoMap(), loginUsername);
                ordDisDeliveryService.saveAsyncImportDeliveryOrder(ordDisDeliveryInList);
            }
            List<OrdDisDeliveryImportErrorResult> errorResultList = disDeliveryImportCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "加盟配销单列表导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDisDeliveryImportErrorResult.class);
        } catch (Exception e) {
            log.error("加盟{}配销单列表导入异常", type, e);
            OrdDisDeliveryImportErrorResult errorResult = new OrdDisDeliveryImportErrorResult();
            errorResult.setErrorMessage("加盟配销单列表导入异常，请检查商品配置");
            String sheetName = "加盟" + type + "配销单列表导入问题清单-导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisDeliveryImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }

    private OrdDisDeliveryImportErrorResult initOrdDisDeliveryImportErrorResult(ImportDisDeliveryOrder importDisDeliveryOrder, String errorMessage) {
        OrdDisDeliveryImportErrorResult ordDisDeliveryImportErrorResult = new OrdDisDeliveryImportErrorResult();
        BeanUtils.copy(importDisDeliveryOrder, ordDisDeliveryImportErrorResult);
        ordDisDeliveryImportErrorResult.setDistributionType(DistributionWaysEnum.getNameByType(ordDisDeliveryImportErrorResult.getDistributionType()));
        ordDisDeliveryImportErrorResult.setErrorMessage(errorMessage);
        return ordDisDeliveryImportErrorResult;
    }

    private DisDeliveryImportCheckOut checkImportSaveDeliveryOrderDetail(List<ImportDisDeliveryOrder> importDisDeliveryOrderList, String loginBizOrgCode) {
        List<OrdDisDeliveryImportErrorResult> errorResultList = Lists.newArrayList();
//        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        Map<String, com.edc.erp.model.out.StockInfoOut> allowDeliveryStockInfoOutMap = new HashMap<>();
        Map<String, StoreInfo> storeInfoMap = new HashMap<>();
        Map<String, List<ImportDisDeliveryOrder>> storeImportMap = importDisDeliveryOrderList.stream().collect(Collectors.groupingBy(ImportDisDeliveryOrder::getStoreCode));
        List<String> storeCodeList = importDisDeliveryOrderList.stream().map(ImportDisDeliveryOrder::getStoreCode).collect(Collectors.toList());
        // 批量获取门店所属渠道组织
        Map<String, String> storeChannelBizMap = storeChannelHandle.findStoreChannelBizOrgCode(new AsyncDeliveryImportInfoIn(storeCodeList, loginBizOrgCode));
        Map<String, OrderGoodsOut> storeOrderGoodsOutMap = new HashMap<>();
        // 获取当前登录人所属+可配仓位
        Map<String, StockInfoOut> loginStockInfoOutMap = stockServer.findByAuthOrg(loginBizOrgCode);
        storeImportMap.forEach((storeCode, list) -> {
            String bizOrgCode = storeChannelBizMap.get(storeCode);
            List<String> goodsCodeList = list.stream().map(ImportDisDeliveryOrder::getGoodsCode).collect(Collectors.toList());
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeCode);
            goodsIn.setBizOrgCode(bizOrgCode);
            goodsIn.setGoodsCodeList(goodsCodeList);
            List<OrderGoodsOut> goodsCodesList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
            if (CollectionUtils.isNotEmpty(goodsCodesList)) {
                goodsCodesList.forEach(orderGoodsOut -> storeOrderGoodsOutMap.put(storeCode + SystemConstant.SHORT_LINE + orderGoodsOut.getGoodsCode(), orderGoodsOut));
            }
        });
        //  处理采购
        List<QueryPurchaseIn> queryPurchaseInList = importDisDeliveryOrderList.stream()
                .filter(detail -> StringUtils.isNotBlank(detail.getPurchaseNo()))
                .filter(distinctByKey(detail -> detail.getGoodsCode() + detail.getPurchaseNo()))
                .map(item -> {
                    QueryPurchaseIn queryPurchaseIn = new QueryPurchaseIn();
                    queryPurchaseIn.setPurchaseNo(item.getPurchaseNo());
                    queryPurchaseIn.setGoodsCode(item.getGoodsCode());
                    return queryPurchaseIn;
                }).collect(Collectors.toList());
        Map<String, QueryPurchaseOut> purchaseGoodsExpiryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(queryPurchaseInList)) {
            Response<List<QueryPurchaseOut>> purResponse = purchaseOrderClient.findValidityInfo(queryPurchaseInList);
            if (!purResponse.isSuccess()) {
                throw new BusinessException("核验采购信息异常：" + purResponse.getMessage());
            }
            if (CollectionUtils.isNotEmpty(purResponse.getData())) {
                purchaseGoodsExpiryMap = purResponse.getData().stream()
                        .collect(Collectors.toMap(queryPurchaseOut -> queryPurchaseOut.getPurchaseNo() + SystemConstant.SHORT_LINE + queryPurchaseOut.getGoodsCode(), Function.identity()));
            }
        }
        List<ImportDisDeliveryOrder> deliveryOrderList = Lists.newArrayList();
        Map<String, QueryPurchaseOut> finalPurchaseGoodsExpiryMap = purchaseGoodsExpiryMap;
        importDisDeliveryOrderList.forEach(importDisDeliveryOrder -> {
            // 判断当前登录人可操作仓位是否匹配当前导入的仓位
            if (!loginStockInfoOutMap.containsKey(importDisDeliveryOrder.getStockCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "仓位" + importDisDeliveryOrder.getStockCode() + "未被授权或不存在"));
                return;
            }
            StockInfoOut stockInfoOut = loginStockInfoOutMap.get(importDisDeliveryOrder.getStockCode());
            // 中心仓业务组织
            String centerStockBizOrgCode = stockInfoOut.getBizOrgCode();
            importDisDeliveryOrder.setCenterStockBizOrgCode(centerStockBizOrgCode);
            // 渠道业务组织
            String storeChannelBizOrgCode = storeChannelBizMap.get(importDisDeliveryOrder.getStoreCode());
//            String bizOrgCode = storeChannelBizMap.get(importDisDeliveryOrder.getStoreCode());
//            if (Objects.isNull(stockInfoOut)) {
//                stockInfoOut = stockServer.getByCode(importDisDeliveryOrder.getStockCode(), bizOrgCode);
//                if (Objects.nonNull(stockInfoOut)) {
//                    stockInfoOutMap.put(importDisDeliveryOrder.getStockCode(), stockInfoOut);
//                } else {
//                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "仓位不存在"));
//                    return;
//                }
//            }
            if (!stockInfoOut.getWarehouseCode().equals(importDisDeliveryOrder.getWrhCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "仓位与仓储不匹配"));
                return;
            }
            com.edc.erp.model.out.StockInfoOut allowDeliveryStockInfo = allowDeliveryStockInfoOutMap.get(importDisDeliveryOrder.getStockCode());
            if (Objects.isNull(allowDeliveryStockInfo)) {
                allowDeliveryStockInfo = ordDisDeliveryDetailService.findStockInfoByCode(importDisDeliveryOrder.getStockCode(), centerStockBizOrgCode);
                if (Objects.nonNull(allowDeliveryStockInfo)) {
                    allowDeliveryStockInfoOutMap.put(importDisDeliveryOrder.getStockCode(), allowDeliveryStockInfo);
                } else {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "仓位不允许进行配货"));
                    return;
                }
            }
            StoreInfo storeInfo = storeInfoMap.get(importDisDeliveryOrder.getStoreCode());
            if (Objects.isNull(storeInfo)) {
                StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
                storeStatusInfo.setBizOrgCode(centerStockBizOrgCode);
                storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
                storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.FRANCHISE.getMytValue());
                storeStatusInfo.setStoreCode(importDisDeliveryOrder.getStoreCode());
                storeInfo = storeCenterService.getStatusStoreInfoByAuthOrg(storeStatusInfo);
                if (Objects.nonNull(storeInfo)) {
                    storeInfoMap.put(importDisDeliveryOrder.getStoreCode(), storeInfo);
                } else {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "门店不存在或者门店不允许配销"));
                    return;
                }
            }
            String goodsCode = importDisDeliveryOrder.getGoodsCode();
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
            orderGoodsIn.setGoodsCode(goodsCode);
            orderGoodsIn.setStoreCode(importDisDeliveryOrder.getStoreCode());
            orderGoodsIn.setBizOrgCode(storeChannelBizOrgCode);
            OrderGoodsOut goodsOut = storeOrderGoodsOutMap.get(importDisDeliveryOrder.getStoreCode() + SystemConstant.SHORT_LINE + goodsCode);
            if (Objects.isNull(goodsOut)) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "商品不可配货"));
                return;
            }
            Integer isDisDis = goodsOut.getGoodsStatusBusinessSwitch().getIsDisDis();
            if (NumberUtil.INTEGER_ZERO.equals(isDisDis)) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "不允许集货配销"));
                return;
            }
            if (Objects.isNull(goodsOut.getDistributionUnitPrice()) || BigDecimal.ZERO.compareTo(goodsOut.getDistributionUnitPrice()) == NumberUtil.INTEGER_ZERO) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "配销价为空"));
                return;
            }
            if (Objects.isNull(goodsOut.getDistributionSpecification())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "配销规格为空"));
                return;
            }
            if (importDisDeliveryOrder.getDeliveryQuantity() % goodsOut.getDistributionSpecification().getQpc() != NumberUtil.INTEGER_ZERO) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "配销数量应为配销规格整数倍"));
                return;
            }
            if (!importDisDeliveryOrder.getStockCode().equals(goodsOut.getStockCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "仓位代码与所选仓位代码不匹配"));
                return;
            }
            if (!importDisDeliveryOrder.getDistributionType().equals(goodsOut.getDistributionWay())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "配货方式与所选配货方式不匹配"));
                return;
            }
            if (StringUtils.isNotBlank(importDisDeliveryOrder.getPurchaseNo())) {
                QueryPurchaseOut queryPurchaseOut = finalPurchaseGoodsExpiryMap.get(importDisDeliveryOrder.getPurchaseNo() + SystemConstant.SHORT_LINE + importDisDeliveryOrder.getGoodsCode());
                if (Objects.isNull(queryPurchaseOut)) {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDisDeliveryOrder, "采购单下此商品不存在或者采购单未审核"));
                    return;
                }
                if (StringUtils.isNotBlank(queryPurchaseOut.getValidityCode())) {
                    importDisDeliveryOrder.setExpiry(queryPurchaseOut.getValidityCode().substring(8, queryPurchaseOut.getValidityCode().length() - 1));
                }
            }
            deliveryOrderList.add(importDisDeliveryOrder);
        });
        DisDeliveryImportCheckOut disDeliveryImportCheckOut = new DisDeliveryImportCheckOut();
        disDeliveryImportCheckOut.setDeliveryOrderList(deliveryOrderList);
        disDeliveryImportCheckOut.setErrorResultList(errorResultList);
        disDeliveryImportCheckOut.setStoreInfoMap(storeInfoMap);
        return disDeliveryImportCheckOut;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
