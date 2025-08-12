package com.edc.erp.directly.returnorder.handle;

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
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderReasonEnum;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.directly.returnorder.model.excel.OrdDirReturnImportErrorResult;
import com.edc.erp.directly.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.directly.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.directly.returnorder.model.out.DirReturnImportCheckOut;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
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
public class DirReturnImportHandle {

    private final StoreCenterService storeCenterService;

    private final StockServer stockServer;

    private final WarehouseServer warehouseServer;

    private final OrderGoodsServer orderGoodsServer;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDirReturnDetailService ordDirReturnDetailService;

    private final RedisService redisService;

    private final StoreChannelHandle storeChannelHandle;

    private final OrdDirDeliveryService ordDirDeliveryService;


    @Async
    public void handleReturnListAsyncImport(List<ImportOrdReturnOrderVO> importDisReturnOrderList, String loginUsername, String loginBizOrgCode, String key) {
        try {
            DirReturnImportCheckOut dirReturnImportCheckOut = this.checkImportSaveReturnOrderDetail(importDisReturnOrderList, loginBizOrgCode);
            List<ImportOrdReturnOrderVO> resultImportReturnList = dirReturnImportCheckOut.getImportOrdReturnOrderVOList();
            if (CollectionUtils.isNotEmpty(resultImportReturnList)) {
                List<OrdSaveReturnOrderIn> ordSaveReturnOrderInList = this.initAsyncImportReturnData(resultImportReturnList,
                        dirReturnImportCheckOut.getStoreGoodsMap(), dirReturnImportCheckOut.getStoreBizOrgCodeMap(), dirReturnImportCheckOut.getDeliverysMap(), loginUsername);
                ordDirReturnDetailService.saveAsyncImportReturn(ordSaveReturnOrderInList);
            }
            List<OrdDirReturnImportErrorResult> errorResultList = dirReturnImportCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "配货退货单列表导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDirReturnImportErrorResult.class);
        } catch (Exception e) {
            log.error("直营退货单列表导入异常", e);
            OrdDirReturnImportErrorResult errorResult = new OrdDirReturnImportErrorResult();
            errorResult.setErrorMessage("直营退货单列表导入异常，请检查商品配置");
            String sheetName = "直营退货单列表导入问题清单-导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirReturnImportErrorResult.class);
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
                                                                Map<String, Map<String, OrdDirDeliveryDetail>> deliverysMap, String loginUsername) {
        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream()
                .collect(Collectors.groupingBy(item -> this.getSplitKey(item, storeGoodsMap.get(item.getStoreCode() + SystemConstant.SHORT_LINE + item.getGoodsCode()))));
        List<OrdSaveReturnOrderIn> list = Lists.newArrayList();
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
            List<OrdDirReturnDetail> ordDisReturnDetailList = new ArrayList<>();
            //明细数据封装
            for (ImportOrdReturnOrderVO importOrdReturnOrderVO : value) {
                OrderGoodsOut orderGoodsOut = storeGoodsMap.get(importOrdReturnOrderVO.getStoreCode() + SystemConstant.SHORT_LINE + importOrdReturnOrderVO.getGoodsCode());
                OrdDirReturnDetail ordDirReturnDetail = ordDirReturnDetailService.initDetail(importOrdReturnOrderVO, bizOrgCode, orderGoodsOut);

                if (StringUtils.isNotBlank(importOrdReturnOrderVO.getDeliveryOrderNo())) {
                    Map<String, OrdDirDeliveryDetail> deliveryDetailMap = deliverysMap.get(importOrdReturnOrderVO.getDeliveryOrderNo());
                    if (null != deliveryDetailMap && deliveryDetailMap.size() > 0) {
                        ordDirReturnDetail.setExpiry(deliveryDetailMap.get(importOrdReturnOrderVO.getGoodsCode()).getExpiry());
                    }
                }
                if (StringUtils.isNotBlank(importOrdReturnOrderVO.getExpiry())) {
                    ordDirReturnDetail.setExpiry(importOrdReturnOrderVO.getExpiry());
                }
                ordDisReturnDetailList.add(ordDirReturnDetail);
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

    private OrdDirReturnImportErrorResult initOrdDirReturnImportErrorResult(ImportOrdReturnOrderVO importOrdReturnOrderVO, String errorMessage) {
        OrdDirReturnImportErrorResult ordDirReturnImportErrorResult = new OrdDirReturnImportErrorResult();
        BeanUtils.copy(importOrdReturnOrderVO, ordDirReturnImportErrorResult);
        ordDirReturnImportErrorResult.setErrorMessage(errorMessage);
        return ordDirReturnImportErrorResult;
    }

    private DirReturnImportCheckOut checkImportSaveReturnOrderDetail(List<ImportOrdReturnOrderVO> importOrdReturnOrderVOList, String loginBizOrgCode) {
        List<OrdDirReturnImportErrorResult> errorResultList = Lists.newArrayList();
//        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        Map<String, WarehouseInfoOut> warehouseInfoOutMap = new HashMap<>();
        Map<String, StoreOut> storeInfoMap = new HashMap<>();
        Map<String, OrderGoodsOut> storeGoodsMap = new HashMap<>();
        List<String> storeCodeList = importOrdReturnOrderVOList.stream().map(ImportOrdReturnOrderVO::getStoreCode).collect(Collectors.toList());
        // 根据当前登录人批量获取可配+所属 的门店的渠道组织
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
        // 配货单明细
        Map<String, Map<String, OrdDirDeliveryDetail>> deliverysMap = new HashMap<>();
        importOrdReturnOrderVOList.forEach(importOrdReturnOrderVO -> {
            // 判断当前登录人可操作仓位是否匹配当前导入的仓位
            if (!loginStockInfoOutMap.containsKey(importOrdReturnOrderVO.getStockCode())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓位" + importOrdReturnOrderVO.getStockCode() + "未被授权或者仓位不存在"));
                return;
            }
            StoreOut storeOut = storeInfoMap.get(importOrdReturnOrderVO.getStoreCode());
            if (Objects.isNull(storeOut)) {
                storeOut = storeCenterService.getStoreInfoByErpStoreCode(importOrdReturnOrderVO.getStoreCode());
            }
            if (Objects.nonNull(storeOut)) {
                storeInfoMap.put(importOrdReturnOrderVO.getStoreCode(), storeOut);
            }
            if (Objects.isNull(storeOut) || !StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeOut.getStoreType())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "门店不存在或类型不匹配"));
                return;
            }

            StockInfoOut stockInfoOut = loginStockInfoOutMap.get(importOrdReturnOrderVO.getStockCode());
            String centerStockBizOrgCode = stockInfoOut.getBizOrgCode();
            importOrdReturnOrderVO.setCenterStockBizOrgCode(centerStockBizOrgCode);
//            StockInfoOut stockInfoOut = stockInfoOutMap.get(importOrdReturnOrderVO.getStockCode());
//            if (Objects.isNull(stockInfoOut)) {
//                stockInfoOut = stockServer.getByCode(importOrdReturnOrderVO.getStockCode(), bizOrgCode);
//            }
//            if (Objects.nonNull(stockInfoOut)) {
//            stockInfoOutMap.put(importOrdReturnOrderVO.getStockCode(), stockInfoOut);
            if (!NumberUtil.INTEGER_ONE.equals(stockInfoOut.getIsEnable())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓位状态为禁用"));
                return;
            }
//            } else {
//                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓位代码不存在"));
//                return;
//            }
            boolean flag = stockServer.getReturnStockStatus(StoreConstant.StoreProperty.DIRECTLY.getMytValue(), importOrdReturnOrderVO.getStockCode(), centerStockBizOrgCode);
            if (!flag) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓位不允许配货退货"));
                return;
            }
            WarehouseInfoOut warehouseInfoOut = warehouseInfoOutMap.get(importOrdReturnOrderVO.getWarehouseCode());
            if (Objects.isNull(warehouseInfoOut)) {
                warehouseInfoOut = warehouseServer.getByCode(importOrdReturnOrderVO.getWarehouseCode(), centerStockBizOrgCode);
                if (Objects.nonNull(warehouseInfoOut)) {
                    warehouseInfoOutMap.put(importOrdReturnOrderVO.getWarehouseCode(), warehouseInfoOut);
                    if (!NumberUtil.INTEGER_ONE.equals(warehouseInfoOut.getIsEnable())) {
                        errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓储状态为禁用"));
                        return;
                    }
                } else {
                    errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "仓储代码不存在"));
                    return;
                }
            }
            boolean checkWarehouseCodeRight = warehouseServer.checkStockIsWarehouseExist(importOrdReturnOrderVO.getWarehouseCode(), importOrdReturnOrderVO.getStockCode(), centerStockBizOrgCode);
            if (!checkWarehouseCodeRight) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, importOrdReturnOrderVO.getStoreCode() + "仓位不在" + importOrdReturnOrderVO.getWarehouseCode() + "仓储下"));
                return;
            }
            OrderGoodsOut orderGoodsOut = storeGoodsMap.get(importOrdReturnOrderVO.getStoreCode() + SystemConstant.SHORT_LINE + importOrdReturnOrderVO.getGoodsCode());
            if (Objects.isNull(orderGoodsOut)) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品不存在/不允许配货退货"));
                return;
            }
            GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = orderGoodsOut.getGoodsStatusBusinessSwitch();
            if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsDisRe())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品不允许做配货退货"));
                return;
            }
            if (!importOrdReturnOrderVO.getWarehouseCode().equals(orderGoodsOut.getReturnWarehouseCode())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品退货仓储和所选退货仓储不符"));
                return;
            }
            if (!importOrdReturnOrderVO.getStockCode().equals(orderGoodsOut.getBackStockCode())) {
                errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品退货仓位和所选仓位不符"));
                return;
            }
            // 判断配货单
            if (StringUtils.isNotBlank(importOrdReturnOrderVO.getDeliveryOrderNo())) {
                OrdDirDeliveryDetail ordDirDeliveryDetail;
                Map<String, OrdDirDeliveryDetail> detailMap = deliverysMap.get(importOrdReturnOrderVO.getDeliveryOrderNo());
                if (Objects.isNull(detailMap)) {
                    OrdDirDeliveryOut ordDirDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(importOrdReturnOrderVO.getDeliveryOrderNo());
                    if (Objects.isNull(ordDirDeliveryOut)) {
                        errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "配货单不存在"));
                        return;
                    }
                    if (NumberUtil.INTEGER_ONE.equals(ordDirDeliveryOut.getIsReversal())) {
                        errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "配货单已冲销"));
                        return;
                    }
                    if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDirDeliveryOut.getDeliveryStatusCode())) {
                        errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "必须是已收货的配货单"));
                        return;
                    }
                    detailMap = new HashMap<>();
                    for (OrdDirDeliveryDetail detail : ordDirDeliveryOut.getDetailList()) {
                        detailMap.put(detail.getGoodsCode(), detail);
                    }
                    deliverysMap.put(importOrdReturnOrderVO.getDeliveryOrderNo(), detailMap);
                }
                ordDirDeliveryDetail = detailMap.get(importOrdReturnOrderVO.getGoodsCode());
                if (Objects.isNull(ordDirDeliveryDetail)) {
                    errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "配销单不含此商品"));
                    return;
                }
                if (Objects.isNull(ordDirDeliveryDetail.getArrivalQuantity())) {
                    errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品实收数为空"));
                    return;
                }
//                if (ordDirDeliveryDetail.getArrivalQuantity().compareTo(importOrdReturnOrderVO.getApplyReturnQuantity()) == -1) {
//                    errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, "商品实收数小于申请退货数"));
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
                    errorResultList.add(this.initOrdDirReturnImportErrorResult(importOrdReturnOrderVO, expiryResponse.getMessage()));
                    return;
                }
            }
            resultList.add(importOrdReturnOrderVO);
        });
        DirReturnImportCheckOut dirReturnImportCheckOut = new DirReturnImportCheckOut();
        dirReturnImportCheckOut.setImportOrdReturnOrderVOList(resultList);
        dirReturnImportCheckOut.setErrorResultList(errorResultList);
        dirReturnImportCheckOut.setStoreGoodsMap(storeGoodsMap);
        dirReturnImportCheckOut.setStoreBizOrgCodeMap(storeChannelBizMap);
        dirReturnImportCheckOut.setDeliverysMap(deliverysMap);
        return dirReturnImportCheckOut;
    }


}
