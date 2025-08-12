package com.edc.erp.returnorder.handle;

import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.ExpiryCheckUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.excel.OrdDisDeliveryImportErrorResult;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderReasonEnum;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.returnorder.model.excel.OrdDisReturnImportErrorResult;
import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.returnorder.model.out.DisReturnImportCheckOut;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
public class DisReturnImportHandle {

    private final StoreCenterService storeCenterService;

    private final StockServer stockServer;

    private final WarehouseServer warehouseServer;

    private final OrderGoodsServer orderGoodsServer;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDisReturnDetailService ordDisReturnDetailService;

    private final RedisService redisService;

    private final StoreChannelHandle storeChannelHandle;

    private final OrdDisDeliveryService ordDisDeliveryService;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;


    @Async
    public void handleReturnListAsyncImport(List<ImportOrdReturnOrderVO> importDisReturnOrderList, String loginUsername, String loginBizOrgCode, String key) {
        try {
            DisReturnImportCheckOut disReturnImportCheckOut = this.checkImportSaveReturnOrderDetail(importDisReturnOrderList, loginBizOrgCode);
            List<ImportOrdReturnOrderVO> resultImportReturnList = disReturnImportCheckOut.getImportOrdReturnOrderVOList();
            if (CollectionUtils.isNotEmpty(resultImportReturnList)) {
                List<OrdSaveReturnOrderIn> ordSaveReturnOrderInList = this.initAsyncImportReturnData(resultImportReturnList,
                        disReturnImportCheckOut.getStoreGoodsMap(), disReturnImportCheckOut.getStoreBizOrgCodeMap(), disReturnImportCheckOut.getDeliverysMap(), loginUsername);
                ordDisReturnDetailService.saveAsyncImportReturn(ordSaveReturnOrderInList);
            }
            List<OrdDisReturnImportErrorResult> errorResultList = disReturnImportCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "配销退货单列表导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDisReturnImportErrorResult.class);
        } catch (Exception e) {
            log.error("配销退货单列表导入异常", e);
            OrdDisDeliveryImportErrorResult errorResult = new OrdDisDeliveryImportErrorResult();
            errorResult.setErrorMessage("配销退货单列表导入异常，请检查商品配置");
            String sheetName = "配销退货单列表导入问题清单-导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisDeliveryImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }

    private String getSplitKey(ImportOrdReturnOrderVO importOrdReturnOrderVO, OrderGoodsOut orderGoodsOut) {
        String key = importOrdReturnOrderVO.getStoreCode() + SystemConstant.COMMA + importOrdReturnOrderVO.getStockCode()
                + SystemConstant.COMMA + importOrdReturnOrderVO.getWarehouseCode() + SystemConstant.COMMA + orderGoodsOut.getDistributionWay();
        return key;
    }

    public List<OrdSaveReturnOrderIn> initAsyncImportReturnData(List<ImportOrdReturnOrderVO> importOrdReturnOrderList,
                                                                Map<String, OrderGoodsOut> storeGoodsMap, Map<String, String> storeBizOrgCodeMap,
                                                                Map<String, Map<String, OrdDisDeliveryDetail>> deliverysMap, String loginUsername) {
        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream()
                .collect(Collectors.groupingBy(item -> this.getSplitKey(item, storeGoodsMap.get(item.getStoreCode() + SystemConstant.SHORT_LINE + item.getGoodsCode()))));
        List<OrdSaveReturnOrderIn> list = com.google.common.collect.Lists.newArrayList();
        for (Map.Entry<String, List<ImportOrdReturnOrderVO>> entry : returnOrderInfo.entrySet()) {
            List<ImportOrdReturnOrderVO> value = entry.getValue();
            String[] splits = entry.getKey().split(SystemConstant.COMMA);
            OrdSaveReturnOrderIn ordSaveReturnOrderIn = new OrdSaveReturnOrderIn();
            ordSaveReturnOrderIn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
            ordSaveReturnOrderIn.setStoreCode(splits[0]);
            ordSaveReturnOrderIn.setStockCode(splits[1]);
            ordSaveReturnOrderIn.setWarehouseCode(splits[2]);
            ordSaveReturnOrderIn.setDistributionType(DistributionWaysEnum.getNameByType(splits[3]));
            ordSaveReturnOrderIn.setReturnOrderReason(OrdReturnOrderReasonEnum.PREVIOUS_RETURN.getDictVlueCode());
            String bizOrgCode = storeBizOrgCodeMap.get(ordSaveReturnOrderIn.getStoreCode());
            ordSaveReturnOrderIn.setBizOrgCode(bizOrgCode);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(splits[0]);
            ordSaveReturnOrderIn.setStoreName(Objects.isNull(storeOut) ? "" : storeOut.getStoreName());
            ordSaveReturnOrderIn.setStoreArea(storeOut.getBelongArea());
            List<OrdDisReturnDetail> ordDisReturnDetailList = new ArrayList<>();
            //明细数据封装
            for (ImportOrdReturnOrderVO importOrdReturnOrderVO : value) {
                OrderGoodsOut orderGoodsOut = storeGoodsMap.get(importOrdReturnOrderVO.getStoreCode() + SystemConstant.SHORT_LINE + importOrdReturnOrderVO.getGoodsCode());
                OrdDisReturnDetail ordDisReturnDetail = ordDisReturnDetailService.initDetail(importOrdReturnOrderVO, bizOrgCode, orderGoodsOut);
                if (StringUtils.isNotBlank(importOrdReturnOrderVO.getDeliveryOrderNo())) {
                    Map<String, OrdDisDeliveryDetail> deliveryDetailMap = deliverysMap.get(importOrdReturnOrderVO.getDeliveryOrderNo());
                    if (null != deliveryDetailMap && deliveryDetailMap.size() > 0) {
                        ordDisReturnDetail.setExpiry(deliveryDetailMap.get(importOrdReturnOrderVO.getGoodsCode()).getExpiry());
                    }
                }
                if (StringUtils.isNotBlank(importOrdReturnOrderVO.getExpiry())) {
                    ordDisReturnDetail.setExpiry(importOrdReturnOrderVO.getExpiry());
                }
                ordDisReturnDetailList.add(ordDisReturnDetail);
            }
            ImportOrdReturnOrderVO importOrdReturnOrderVO = value.get(NumberUtil.INTEGER_ZERO);
            ordSaveReturnOrderIn.setReturnGoodsInfoInList(ordDisReturnDetailList);
            ordSaveReturnOrderIn.setLoginUsername(loginUsername);
            ordSaveReturnOrderIn.setCenterStockBizOrgCode(importOrdReturnOrderVO.getCenterStockBizOrgCode());
            ordSaveReturnOrderIn.setDeliveryOrderNo(importOrdReturnOrderVO.getDeliveryOrderNo());
            list.add(ordSaveReturnOrderIn);
        }
        return list;
    }

    private OrdDisReturnImportErrorResult initOrdDisReturnImportErrorResult(ImportOrdReturnOrderVO importOrdReturnOrderVO, String errorMessage) {
        OrdDisReturnImportErrorResult ordDisDeliveryImportErrorResult = new OrdDisReturnImportErrorResult();
        BeanUtils.copy(importOrdReturnOrderVO, ordDisDeliveryImportErrorResult);
        ordDisDeliveryImportErrorResult.setErrorMessage(errorMessage);
        return ordDisDeliveryImportErrorResult;
    }

    private DisReturnImportCheckOut checkImportSaveReturnOrderDetail(List<ImportOrdReturnOrderVO> importOrdReturnOrderVOList, String loginBizOrgCode) {
        List<OrdDisReturnImportErrorResult> errorResultList = Lists.newArrayList();
//        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        Map<String, WarehouseInfoOut> warehouseInfoOutMap = new HashMap<>();
        Map<String, StoreOut> storeInfoMap = new HashMap<>();
        Map<String, OrderGoodsOut> storeGoodsMap = new HashMap<>();
        List<String> storeCodeList = importOrdReturnOrderVOList.stream().map(ImportOrdReturnOrderVO::getStoreCode).collect(Collectors.toList());
        // 批量获取门店所属渠道组织
        Map<String, String> storeChannelBizMap = storeChannelHandle.findStoreChannelBizOrgCode(new AsyncDeliveryImportInfoIn(storeCodeList, loginBizOrgCode));
        // 获取当前登录人所属+可配仓位
        Map<String, StockInfoOut> loginStockInfoOutMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Map<String, List<ImportOrdReturnOrderVO>> storeMap = importOrdReturnOrderVOList.stream().collect(Collectors.groupingBy(ImportOrdReturnOrderVO::getStoreCode));
        storeMap.entrySet().forEach(entry -> {
            String storeChannelBizOrgCode = storeChannelBizMap.get(entry.getKey());
            List<String> goodsCodeList = entry.getValue().stream().map(ImportOrdReturnOrderVO::getGoodsCode).collect(Collectors.toList());
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(entry.getKey());
            goodsIn.setBizOrgCode(storeChannelBizOrgCode);
            goodsIn.setGoodsCodeList(goodsCodeList);
            List<OrderGoodsOut> orderGoodsOutList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
            if (CollectionUtils.isNotEmpty(orderGoodsOutList)) {
                Map<String, OrderGoodsOut> goodsMap = orderGoodsOutList.stream().collect(Collectors.toMap(orderGoodsOut -> entry.getKey() + SystemConstant.SHORT_LINE + orderGoodsOut.getGoodsCode(), Function.identity()));
                storeGoodsMap.putAll(goodsMap);
            }
        });
        List<ImportOrdReturnOrderVO> resultList = Lists.newArrayList();
        // 配销单明细
        Map<String, Map<String, OrdDisDeliveryDetail>> deliverysMap = new HashMap<>();
        importOrdReturnOrderVOList.forEach(importOrdReturnOrderVO -> {
            // 判断当前登录人可操作仓位是否匹配当前导入的仓位
            if (!loginStockInfoOutMap.containsKey(importOrdReturnOrderVO.getStockCode())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓位" + importOrdReturnOrderVO.getStockCode() + "未被授权货不存在"));
                return;
            }
            StoreOut storeOut = storeInfoMap.get(importOrdReturnOrderVO.getStoreCode());
            if (Objects.isNull(storeOut)) {
                storeOut = storeCenterService.getStoreInfoByErpStoreCode(importOrdReturnOrderVO.getStoreCode());
            }
            if (Objects.nonNull(storeOut)) {
                storeInfoMap.put(importOrdReturnOrderVO.getStoreCode(), storeOut);
            }
            if (Objects.isNull(storeOut) || !StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeOut.getStoreType())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "门店不存在或类型不匹配"));
                return;
            }
            StockInfoOut stockInfoOut = loginStockInfoOutMap.get(importOrdReturnOrderVO.getStockCode());
            String centerStockBizOrgCode = stockInfoOut.getBizOrgCode();
            importOrdReturnOrderVO.setCenterStockBizOrgCode(centerStockBizOrgCode);
//            StockInfoOut stockInfoOut = loginStockInfoOutMap.get(importOrdReturnOrderVO.getStockCode());
//            if (Objects.isNull(stockInfoOut)) {
//                stockInfoOut = stockServer.getByCode(importOrdReturnOrderVO.getStockCode(), bizOrgCode);
//            }
//            if (Objects.nonNull(stockInfoOut)) {
//            stockInfoOutMap.put(importOrdReturnOrderVO.getStockCode(), stockInfoOut);
            if (!NumberUtil.INTEGER_ONE.equals(stockInfoOut.getIsEnable())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓位状态为禁用"));
                return;
            }
//            } else {
//                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓位代码不存在"));
//                return;
//            }
            boolean flag = stockServer.getReturnStockStatus(StoreConstant.StoreProperty.FRANCHISE.getMytValue(), importOrdReturnOrderVO.getStockCode(), centerStockBizOrgCode);
            if (!flag) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓位不允许配销退货"));
                return;
            }
            WarehouseInfoOut warehouseInfoOut = warehouseInfoOutMap.get(importOrdReturnOrderVO.getWarehouseCode());
            if (Objects.isNull(warehouseInfoOut)) {
                warehouseInfoOut = warehouseServer.getByCode(importOrdReturnOrderVO.getWarehouseCode(), centerStockBizOrgCode);
                if (Objects.nonNull(warehouseInfoOut)) {
                    warehouseInfoOutMap.put(importOrdReturnOrderVO.getWarehouseCode(), warehouseInfoOut);
                    if (!NumberUtil.INTEGER_ONE.equals(warehouseInfoOut.getIsEnable())) {
                        errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓储状态为禁用"));
                        return;
                    }
                } else {
                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "仓储代码不存在"));
                    return;
                }
            }
            boolean checkWarehouseCodeRight = warehouseServer.checkStockIsWarehouseExist(importOrdReturnOrderVO.getWarehouseCode(), importOrdReturnOrderVO.getStockCode(), centerStockBizOrgCode);
            if (!checkWarehouseCodeRight) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, importOrdReturnOrderVO.getStoreCode() + "仓位不在" + importOrdReturnOrderVO.getWarehouseCode() + "仓储下"));
                return;
            }
            OrderGoodsOut orderGoodsOut = storeGoodsMap.get(importOrdReturnOrderVO.getStoreCode() + SystemConstant.SHORT_LINE + importOrdReturnOrderVO.getGoodsCode());
            if (Objects.isNull(orderGoodsOut)) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品不存在/不允许配销退货"));
                return;
            }
            GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = orderGoodsOut.getGoodsStatusBusinessSwitch();
            if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsDisReDis())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品不允许做配销退货"));
                return;
            }
            if (!importOrdReturnOrderVO.getWarehouseCode().equals(orderGoodsOut.getReturnWarehouseCode())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品退货仓储和所选退货仓储不符"));
                return;
            }
            if (!importOrdReturnOrderVO.getStockCode().equals(orderGoodsOut.getBackStockCode())) {
                errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品退货仓位和所选仓位不符"));
                return;
            }
            // 判断配货单
            if (StringUtils.isNotBlank(importOrdReturnOrderVO.getDeliveryOrderNo())) {
                OrdDisDeliveryDetail ordDisDeliveryDetail;
                Map<String, OrdDisDeliveryDetail> detailMap = deliverysMap.get(importOrdReturnOrderVO.getDeliveryOrderNo());
                if (Objects.isNull(detailMap)) {
                    OrdDisDeliveryOut ordDisDeliveryOut = ordDisDeliveryService.getDeliveryOrderOutByNo(importOrdReturnOrderVO.getDeliveryOrderNo());
                    if (Objects.isNull(ordDisDeliveryOut)) {
                        errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "配销单不存在"));
                        return;
                    }
                    if (NumberUtil.INTEGER_ONE.equals(ordDisDeliveryOut.getIsReversal())) {
                        errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "配销单已冲销"));
                        return;
                    }
                    if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDeliveryOut.getDeliveryStatusCode())) {
                        errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "必须是已收货的配销单"));
                        return;
                    }
                    detailMap = new HashMap<>();
                    for (OrdDisDeliveryDetail detail : ordDisDeliveryOut.getDetailList()) {
                        detailMap.put(detail.getGoodsCode(), detail);
                    }
                    deliverysMap.put(importOrdReturnOrderVO.getDeliveryOrderNo(), detailMap);
                }
                ordDisDeliveryDetail = detailMap.get(importOrdReturnOrderVO.getGoodsCode());
                if (Objects.isNull(ordDisDeliveryDetail)) {
                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "配销单不含此商品"));
                    return;
                }
                if (Objects.isNull(ordDisDeliveryDetail.getArrivalQuantity())) {
                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品实收数为空"));
                    return;
                }
//                if (ordDisDeliveryDetail.getArrivalQuantity().compareTo(importOrdReturnOrderVO.getApplyReturnQuantity()) == -1) {
//                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "商品实收数小于申请退货数"));
//                    return;
//                }
            }
            if (StringUtils.isBlank(importOrdReturnOrderVO.getDeliveryOrderNo()) && StringUtils.isNotBlank(importOrdReturnOrderVO.getExpiry())) {
//                if (NumberUtil.INTEGER_ONE.equals(orderGoodsOut.getIsManageValidityPeriod()) && StringUtils.isBlank(importOrdReturnOrderVO.getExpiry())) {
//                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, "效期商品效期码为空"));
//                    return;
//                }
                Response<LocalDateTime> expiryResponse = ExpiryCheckUtil.checkExpiry(importOrdReturnOrderVO.getExpiry());
                if (!expiryResponse.isSuccess()) {
                    errorResultList.add(this.initOrdDisReturnImportErrorResult(importOrdReturnOrderVO, expiryResponse.getMessage()));
                    return;
                }
            }
            resultList.add(importOrdReturnOrderVO);
        });
        DisReturnImportCheckOut disReturnImportCheckOut = new DisReturnImportCheckOut();
        disReturnImportCheckOut.setImportOrdReturnOrderVOList(resultList);
        disReturnImportCheckOut.setErrorResultList(errorResultList);
        disReturnImportCheckOut.setStoreGoodsMap(storeGoodsMap);
        disReturnImportCheckOut.setStoreBizOrgCodeMap(storeChannelBizMap);
        disReturnImportCheckOut.setDeliverysMap(deliverysMap);
        return disReturnImportCheckOut;
    }


}
