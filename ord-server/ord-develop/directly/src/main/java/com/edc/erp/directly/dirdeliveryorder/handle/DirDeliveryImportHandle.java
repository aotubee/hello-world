package com.edc.erp.directly.dirdeliveryorder.handle;

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
import com.edc.erp.directly.dirdeliveryorder.model.excel.OrdDirDeliveryImportErrorResult;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDirDeliveryOrder;
import com.edc.erp.directly.dirdeliveryorder.model.in.OrdDirDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.DirDeliveryImportCheckOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
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
public class DirDeliveryImportHandle {

    private final OrdDirDeliveryService ordDirDeliveryService;

    private final OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    private final StoreCenterService storeCenterService;

    private final OrderGoodsServer orderGoodsServer;

    private final StockServer stockServer;

    private final AsyncExportHandle asyncExportHandle;

    private final RedisService redisService;

    private final StoreChannelHandle storeChannelHandle;

    private final PurchaseOrderClient purchaseOrderClient;


    @Async
    public void handleDeliveryListAsyncImport(List<ImportDirDeliveryOrder> importDirDeliveryOrderList, String loginUsername, String loginBizOrgCode, String type, String key) {
        try {
            DirDeliveryImportCheckOut disDeliveryImportCheckOut = this.checkImportSaveDeliveryOrderDetail(importDirDeliveryOrderList, loginBizOrgCode);
            List<ImportDirDeliveryOrder> deliveryOrderList = disDeliveryImportCheckOut.getDeliveryOrderList();
            if (CollectionUtils.isNotEmpty(deliveryOrderList)) {
                List<OrdDirDeliveryIn> ordDirDeliveryInList = ordDirDeliveryService.initDeliveryOrderAndDetailForAsyncImport(deliveryOrderList,
                        disDeliveryImportCheckOut.getStoreInfoMap(), loginUsername);
                ordDirDeliveryService.saveAsyncImportDeliveryOrder(ordDirDeliveryInList);
            }
            List<OrdDirDeliveryImportErrorResult> errorResultList = disDeliveryImportCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "直营" + type + "配货单列表导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDirDeliveryImportErrorResult.class);
        } catch (Exception e) {
            log.error("直营" + type + "配货单列表导入异常", e);
            OrdDirDeliveryImportErrorResult errorResult = new OrdDirDeliveryImportErrorResult();
            errorResult.setErrorMessage("直营配货单列表导入异常，请检查商品配置");
            String sheetName = "直营" + type + "配货单列表导入问题清单-导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirDeliveryImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }

    private OrdDirDeliveryImportErrorResult initOrdDisDeliveryImportErrorResult(ImportDirDeliveryOrder importDisDeliveryOrder, String errorMessage) {
        OrdDirDeliveryImportErrorResult ordDirDeliveryImportErrorResult = new OrdDirDeliveryImportErrorResult();
        BeanUtils.copy(importDisDeliveryOrder, ordDirDeliveryImportErrorResult);
        ordDirDeliveryImportErrorResult.setDistributionType(DistributionWaysEnum.getNameByType(ordDirDeliveryImportErrorResult.getDistributionType()));
        ordDirDeliveryImportErrorResult.setErrorMessage(errorMessage);
        return ordDirDeliveryImportErrorResult;
    }

    private DirDeliveryImportCheckOut checkImportSaveDeliveryOrderDetail(List<ImportDirDeliveryOrder> importDirDeliveryOrderList, String loginBizOrgCode) {
        List<OrdDirDeliveryImportErrorResult> errorResultList = Lists.newArrayList();
//        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        Map<String, StockInfoOut> allowDeliveryStockInfoOutMap = new HashMap<>();
        Map<String, StoreInfo> storeInfoMap = new HashMap<>();
        Map<String, List<ImportDirDeliveryOrder>> storeImportMap = importDirDeliveryOrderList.stream().collect(Collectors.groupingBy(ImportDirDeliveryOrder::getStoreCode));
        List<String> storeCodeList = importDirDeliveryOrderList.stream().map(ImportDirDeliveryOrder::getStoreCode).collect(Collectors.toList());
        // 批量获取门店所属渠道组织
        Map<String, String> storeChannelBizMap = storeChannelHandle.findStoreChannelBizOrgCode(new AsyncDeliveryImportInfoIn(storeCodeList, loginBizOrgCode));
        Map<String, OrderGoodsOut> storeOrderGoodsOutMap = new HashMap<>();
        // 获取当前登录人所属+可配仓位
        Map<String, StockInfoOut> loginStockInfoOutMap = stockServer.findByAuthOrg(loginBizOrgCode);
        storeImportMap.forEach((storeCode, list) -> {
            String storeChannelBizOrgCode = storeChannelBizMap.get(storeCode);
            List<String> goodsCodeList = list.stream().map(ImportDirDeliveryOrder::getGoodsCode).collect(Collectors.toList());
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeCode);
            goodsIn.setBizOrgCode(storeChannelBizOrgCode);
            goodsIn.setGoodsCodeList(goodsCodeList);
            List<OrderGoodsOut> goodsCodesList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
            if (CollectionUtils.isNotEmpty(goodsCodesList)) {
                goodsCodesList.forEach(orderGoodsOut -> storeOrderGoodsOutMap.put(storeCode + SystemConstant.SHORT_LINE + orderGoodsOut.getGoodsCode(), orderGoodsOut));
            }
        });
        //  处理采购
        List<QueryPurchaseIn> queryPurchaseInList = importDirDeliveryOrderList.stream()
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
        List<ImportDirDeliveryOrder> deliveryOrderList = Lists.newArrayList();
        Map<String, QueryPurchaseOut> finalPurchaseGoodsExpiryMap = purchaseGoodsExpiryMap;
        importDirDeliveryOrderList.forEach(importDirDeliveryOrder -> {
            // 判断当前登录人可操作仓位是否匹配当前导入的仓位
            if (!loginStockInfoOutMap.containsKey(importDirDeliveryOrder.getStockCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "仓位" + importDirDeliveryOrder.getStockCode() + "未被授权或仓位不存在"));
                return;
            }
//            StockInfoOut stockInfoOut = stockInfoOutMap.get(importDirDeliveryOrder.getStockCode());
            StockInfoOut stockInfoOut = loginStockInfoOutMap.get(importDirDeliveryOrder.getStockCode());
            // 中心仓业务组织
            String centerStockBizOrgCode = stockInfoOut.getBizOrgCode();
            importDirDeliveryOrder.setCenterStockBizOrgCode(centerStockBizOrgCode);
            // 渠道业务组织
            String storeChannelBizOrgCode = storeChannelBizMap.get(importDirDeliveryOrder.getStoreCode());
//            if (Objects.isNull(stockInfoOut)) {
//                stockInfoOut = stockServer.getByCode(importDirDeliveryOrder.getStockCode(), storeChannelBizOrgCode);
//                if (Objects.nonNull(stockInfoOut)) {
//                    stockInfoOutMap.put(importDirDeliveryOrder.getStockCode(), stockInfoOut);
//                } else {
//                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "仓位不存在"));
//                    return;
//                }
//            }
            if (!stockInfoOut.getWarehouseCode().equals(importDirDeliveryOrder.getWrhCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "仓位与仓储不匹配"));
                return;
            }
            StockInfoOut allowDeliveryStockInfo = allowDeliveryStockInfoOutMap.get(importDirDeliveryOrder.getStockCode());
            if (Objects.isNull(allowDeliveryStockInfo)) {
                allowDeliveryStockInfo = ordDirDeliveryDetailService.findStockInfoByCode(importDirDeliveryOrder.getStockCode(), centerStockBizOrgCode);
                if (Objects.nonNull(allowDeliveryStockInfo)) {
                    allowDeliveryStockInfoOutMap.put(importDirDeliveryOrder.getStockCode(), allowDeliveryStockInfo);
                } else {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "仓位不允许进行配货"));
                    return;
                }
            }
            StoreInfo storeInfo = storeInfoMap.get(importDirDeliveryOrder.getStoreCode());
            if (Objects.isNull(storeInfo)) {
                StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
                storeStatusInfo.setBizOrgCode(centerStockBizOrgCode);
                storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
                storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.DIRECTLY.getMytValue());
                storeStatusInfo.setStoreCode(importDirDeliveryOrder.getStoreCode());
                storeInfo = storeCenterService.getStatusStoreInfoByAuthOrg(storeStatusInfo);
                if (Objects.nonNull(storeInfo)) {
                    storeInfoMap.put(importDirDeliveryOrder.getStoreCode(), storeInfo);
                } else {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "门店不存在或者门店不允许配货"));
                    return;
                }
            }
            String goodsCode = importDirDeliveryOrder.getGoodsCode();
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
            orderGoodsIn.setGoodsCode(goodsCode);
            orderGoodsIn.setStoreCode(importDirDeliveryOrder.getStoreCode());
            orderGoodsIn.setBizOrgCode(storeChannelBizOrgCode);
            OrderGoodsOut goodsOut = storeOrderGoodsOutMap.get(importDirDeliveryOrder.getStoreCode() + SystemConstant.SHORT_LINE + goodsCode);
            if (Objects.isNull(goodsOut)) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "商品不可配货"));
                return;
            }
            Integer isDisDis = goodsOut.getGoodsStatusBusinessSwitch().getIsDis();
            if (NumberUtil.INTEGER_ZERO.equals(isDisDis)) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "不允许要货配货"));
                return;
            }
            if (Objects.isNull(goodsOut.getDistributionUnitPrice()) || BigDecimal.ZERO.compareTo(goodsOut.getDistributionUnitPrice()) == NumberUtil.INTEGER_ZERO) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "配货价为空"));
                return;
            }
            if (Objects.isNull(goodsOut.getDistributionSpecification())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "配货规格为空"));
                return;
            }
            if (importDirDeliveryOrder.getDeliveryQuantity() % goodsOut.getDistributionSpecification().getQpc() != NumberUtil.INTEGER_ZERO) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "配货数量应为配货规格整数倍"));
                return;
            }
            if (!importDirDeliveryOrder.getStockCode().equals(goodsOut.getStockCode())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "仓位代码与所选仓位代码不匹配"));
                return;
            }
            if (!importDirDeliveryOrder.getDistributionType().equals(goodsOut.getDistributionWay())) {
                errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "配货方式与所选配货方式不匹配"));
                return;
            }
            if (StringUtils.isNotBlank(importDirDeliveryOrder.getPurchaseNo())) {
                QueryPurchaseOut queryPurchaseOut = finalPurchaseGoodsExpiryMap.get(importDirDeliveryOrder.getPurchaseNo() + SystemConstant.SHORT_LINE + importDirDeliveryOrder.getGoodsCode());
                if (Objects.isNull(queryPurchaseOut)) {
                    errorResultList.add(this.initOrdDisDeliveryImportErrorResult(importDirDeliveryOrder, "采购单下此商品不存在或者采购单未审核"));
                    return;
                }
                if (StringUtils.isNotBlank(queryPurchaseOut.getValidityCode())) {
                    importDirDeliveryOrder.setExpiry(queryPurchaseOut.getValidityCode().substring(8, queryPurchaseOut.getValidityCode().length() - 1));
                }
            }
            deliveryOrderList.add(importDirDeliveryOrder);
        });
        DirDeliveryImportCheckOut disDeliveryImportCheckOut = new DirDeliveryImportCheckOut();
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
