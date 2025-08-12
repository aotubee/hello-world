//package com.edc.erp.disdeliveryorder.handle;
//
//import com.edc.erp.common.constant.SystemConstant;
//import com.edc.erp.common.enumeration.*;
//import com.edc.erp.common.model.in.zk.*;
//import com.edc.erp.common.model.out.stock.StockInfoOut;
//import com.edc.erp.common.service.StockServer;
//import com.edc.erp.common.util.NumberUtil;
//import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
//import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
//import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderGoodsIn;
//import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderIn;
//import com.edc.erp.disdeliveryorder.model.in.UpdateDisDeliveryDetailIn;
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
//import com.edc.erp.enumeration.ReturnOrderLogEnum;
//import com.edc.erp.handle.TakeDisDeliveryHandle;
//import com.edc.erp.returnorder.entity.OrdDisReturn;
//import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
//import com.edc.erp.returnorder.enumeration.OrdReturnOrderStatusEnum;
//import com.edc.erp.returnorder.model.in.OrdSaveReturnOrderIn;
//import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
//import com.edc.erp.returnorder.service.OrdDisReturnService;
//import com.edc.plugins.common.exception.BusinessException;
//import com.edc.plugins.common.response.Response;
//import com.edc.sdk.log.dto.BusinessLog;
//import com.edc.sdk.log.service.AsyncLogService;
//import com.google.common.collect.Lists;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.text.MessageFormat;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * @ClassName ZKBusinessHandle
// * @Description TODO
// * @Author ZhangYao
// * @CreateTime 2023/8/14 16:09
// **/
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ZKBusinessHandle {
//
//    private final OrdDisDeliveryService ordDisDeliveryService;
//    private final AsyncLogService asyncLogService;
//    private final OrdDisReturnService ordDisReturnService;
//    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;
//    private final TakeDisDeliveryHandle takeDisDeliveryHandle;
//    private final OrdDisReturnDetailService ordDisReturnDetailService;
//    private final StockServer stockServer;
//
//    /**
//     * @param handleDeliveryOrderAuditIn:
//     * @Description: 处理中科配销单审核回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/10 13:38
//     * @return: void
//     **/
//    public void handleZkDeliveryOrderAudit(HandleDeliveryOrderAuditIn handleDeliveryOrderAuditIn) {
//        OrdDisDelivery deliveryOrder = ordDisDeliveryService.getDeliveryOrderOutByNo(handleDeliveryOrderAuditIn.getDeliveryOrderNo());
//        if (Objects.isNull(deliveryOrder)) {
//            log.error("中科配货单审核回调，未找到指定配货单{}", handleDeliveryOrderAuditIn.getDeliveryOrderNo());
//            return;
//        }
//        if (CollectionUtils.isEmpty(handleDeliveryOrderAuditIn.getHandleDeliveryOrderDetailAuditList())) {
//            log.info("中科配销单{}回传明细为空", handleDeliveryOrderAuditIn.getDeliveryOrderNo());
//            return;
//        }
//        long count = handleDeliveryOrderAuditIn.getHandleDeliveryOrderDetailAuditList().stream()
//                .filter(handleDeliveryOrderDetailAuditIn -> Objects.isNull(handleDeliveryOrderDetailAuditIn.getDistributionQuantity())
//                        || handleDeliveryOrderDetailAuditIn.getDistributionQuantity().compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO).count();
//        if (count == handleDeliveryOrderAuditIn.getHandleDeliveryOrderDetailAuditList().size()) {
//            log.info("中科配销单{}回传明细均无审核数", handleDeliveryOrderAuditIn.getDeliveryOrderNo());
//            String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ZK_AUDIT_BACK_NO_FAIL.getKey(), deliveryOrder.getDeliveryOrderNo());
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
//                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
//            ordDisDeliveryService.invalidDisDeliverOrder(deliveryOrder.getId(), SystemConstant.SYSTEM_USER, true);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        } else {
//            String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ZK_AUDIT_BACK_SUCCESS.getKey(), deliveryOrder.getDeliveryOrderNo());
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
//                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
//    }
//
//    /**
//     * @param handleReturnOrderAuditIn:
//     * @Description: 处理中科退货单审核回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/10 19:24
//     * @return: void
//     **/
//    public void handleZkReturnOrderAudit(HandleReturnOrderAuditIn handleReturnOrderAuditIn) {
//        OrdDisReturn ordDisReturn = ordDisReturnService.getReturnOrderByNo(handleReturnOrderAuditIn.getReturnOrderNo());
//        if (Objects.isNull(ordDisReturn)) {
//            log.error("中科退货单审核回调，未找到指定退货单{}", handleReturnOrderAuditIn.getReturnOrderNo());
//            return;
//        }
//        if (1 == handleReturnOrderAuditIn.getAuditResult()) {
//            log.info("中科审核退货单{}未通过", handleReturnOrderAuditIn.getReturnOrderNo());
//            return;
//        }
//        if (CollectionUtils.isEmpty(handleReturnOrderAuditIn.getHandleReturnOrderDetailAuditList())) {
//            log.info("中科审核退货单{}回传明细为空", handleReturnOrderAuditIn.getReturnOrderNo());
//            return;
//        }
//        long count = handleReturnOrderAuditIn.getHandleReturnOrderDetailAuditList().stream()
//                .filter(handleReturnOrderDetailAuditIn -> Objects.isNull(handleReturnOrderDetailAuditIn.getRatifyReturnQuantity())
//                        || handleReturnOrderDetailAuditIn.getRatifyReturnQuantity().compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO)
//                .count();
////        ordDisReturn.setUpdater(SystemConstant.SYSTEM_USER);
////        ordDisReturn.setUpdateTime(LocalDateTime.now());
////        ordDisReturn.setAuditReturnQuantity(null == ordDisReturn.getAuditReturnQuantity() ? BigDecimal.ZERO : ordDisReturn.getAuditReturnQuantity());
////        ordDisReturn.setAuditReturnAmount(null == ordDisReturn.getAuditReturnAmount() ? BigDecimal.ZERO : ordDisReturn.getAuditReturnAmount());
//        if (count == handleReturnOrderAuditIn.getHandleReturnOrderDetailAuditList().size()) {
//            log.info("中科审核退货单{}回传明细均无审核数", handleReturnOrderAuditIn.getReturnOrderNo());
//            String content = MessageFormat.format(ReturnOrderLogEnum.DIS_RETURN_ZK_AUDIT_BACK_NO_FAIL.getKey(), ordDisReturn.getReturnOrderNo());
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
//                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), ordDisReturn.getUpdater());
//            ordDisReturnService.invalidatedOrdReturn(ordDisReturn);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        } else {
//            Map<String, BigDecimal> zkReturnGoodsMap = handleReturnOrderAuditIn.getHandleReturnOrderDetailAuditList().stream()
//                    .collect(Collectors.toMap(handleReturnOrderDetailAuditIn -> this.getZkCallBackGoodsKey(handleReturnOrderDetailAuditIn.getGoodsCode(), handleReturnOrderDetailAuditIn.getLine())
//                            , HandleReturnOrderDetailAuditIn::getRatifyReturnQuantity, (v1, v2) -> v1));
////            Map<String, BigDecimal> zkReturnGoodsMap = handleReturnOrderAuditIn.getHandleReturnOrderDetailAuditList().stream()
////                    .collect(Collectors.toMap(HandleReturnOrderDetailAuditIn::getGoodsCode, HandleReturnOrderDetailAuditIn::getRatifyReturnQuantity, (v1, v2) -> v1));
//            List<OrdDisReturnDetail> dbReturnOrderDetails = ordDisReturnDetailService.findByReturnOrderId(ordDisReturn.getId());
//            List<OrdDisReturnDetail> needUpdateList = Lists.newArrayList();
//            dbReturnOrderDetails.forEach(ordDisReturnDetail -> {
//                // 过滤没有映射的
//                if (Objects.isNull(ordDisReturnDetail.getOtherGoodsCode())) {
//                    log.error("中科退货单{}商品{}没有映射", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
//                    return;
//                }
//                String zkGoodsKey = this.getZkCallBackGoodsKey(ordDisReturnDetail.getOtherGoodsCode(), ordDisReturnDetail.getLine());
//                BigDecimal auditReturnQuantity = zkReturnGoodsMap.get(zkGoodsKey);
//                if (Objects.isNull(auditReturnQuantity)) {
//                    zkGoodsKey = this.getZkCallBackGoodsKey(ordDisReturnDetail.getOtherGoodsCode(), null);
//                    auditReturnQuantity = zkReturnGoodsMap.get(zkGoodsKey);
//                }
//                if (null == auditReturnQuantity) {
//                    auditReturnQuantity = BigDecimal.ZERO;
//                    OrdDisReturnDetail updateOrdDisReturnDetail = new OrdDisReturnDetail();
//                    updateOrdDisReturnDetail.setId(ordDisReturnDetail.getId());
//                    updateOrdDisReturnDetail.setAuditReturnQuantity(auditReturnQuantity);
//                    BigDecimal auditPackageQuantity = auditReturnQuantity.divide(ordDisReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN);
//                    updateOrdDisReturnDetail.setAuditPackageQuantity(auditPackageQuantity);
//                    BigDecimal auditReturnAmount = ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getAuditReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
//                    updateOrdDisReturnDetail.setAuditReturnAmount(auditReturnAmount);
//                    updateOrdDisReturnDetail.setUpdater(SystemConstant.SYSTEM_USER);
//                    updateOrdDisReturnDetail.setUpdateTime(LocalDateTime.now());
//                    needUpdateList.add(updateOrdDisReturnDetail);
////                    ordDisReturn.setAuditReturnQuantity(ordDisReturn.getAuditReturnQuantity().add(updateOrdDisReturnDetail.getAuditReturnQuantity()));
////                    ordDisReturn.setAuditReturnAmount(ordDisReturn.getAuditReturnAmount().add(auditReturnAmount));
//                }
//            });
//            if (CollectionUtils.isNotEmpty(needUpdateList)) {
//                ordDisReturnDetailService.batchUpdate(needUpdateList);
//            }
////            ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.APPROVED.getKey());
////            ordDisReturnService.updateReturnOrder(ordDisReturn);
//            String content = MessageFormat.format(ReturnOrderLogEnum.DIS_RETURN_ZK_AUDIT_BACK_SUCCESS.getKey(), ordDisReturn.getReturnOrderNo());
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
//                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
//    }
//
//    private String getZkCallBackGoodsKey(String otherGoodsCode, Integer line) {
//        if (Objects.isNull(line)) {
//            return otherGoodsCode;
//        } else {
//            return otherGoodsCode + SystemConstant.SHORT_LINE + line;
//        }
//    }
//
//    /**
//     * @param handleDeliveryOrderIn:
//     * @Description: 中科发货回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/14 16:17
//     * @return: com.edc.plugins.common.response.Response<java.lang.String>
//     **/
//    @Transactional(rollbackFor = Exception.class)
//    public Response<String> zkShipped(HandleDeliveryOrderIn handleDeliveryOrderIn) {
//        OrdDisDelivery deliveryOrder = ordDisDeliveryService.getOneByDeliveryOrderNoAndBizOrgCode(handleDeliveryOrderIn.getDeliveryOrderNo(),
//                OrgCodeConvertEnum.getBizOrgCodeByOrgCode(handleDeliveryOrderIn.getOrgCode()));
//        if (Objects.isNull(deliveryOrder)) {
//            log.error("不存在的配货单{}", handleDeliveryOrderIn.getDeliveryOrderNo());
//            return Response.error("不存在的配货单");
//        }
//        if (!DeliveryOrderEnum.APPROVED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
//            log.error("配货单{}状态不正确", handleDeliveryOrderIn.getDeliveryOrderNo());
//            return Response.error("配货单状态不正确");
//        }
//        List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(deliveryOrder.getId());
//        if (CollectionUtils.isEmpty(disDeliveryDetailList)) {
//            return Response.error("配销单{}没有明细", deliveryOrder.getDeliveryOrderNo());
//        }
//        StockInfoOut stockInfoOut = stockServer.getByCode(deliveryOrder.getStockCode(), deliveryOrder.getBizOrgCode());
//        if (Objects.isNull(stockInfoOut)) {
//            return Response.error("无此仓位信息", deliveryOrder.getDeliveryOrderNo());
//        }
//
//        List<HandleDeliveryOrderDetailIn> goodsDetailList = handleDeliveryOrderIn.getGoodsDetailList();
//        Map<String, BigDecimal> zkGoodsQuantityMap;
//        if (CollectionUtils.isEmpty(goodsDetailList)) {
//            zkGoodsQuantityMap = new HashMap<>();
//        } else {
//            zkGoodsQuantityMap = goodsDetailList.stream().collect(Collectors.toMap(handleDeliveryOrderDetailIn ->
//                    this.getZkCallBackGoodsKey(handleDeliveryOrderDetailIn.getGoodsCode(), handleDeliveryOrderDetailIn.getLine()), HandleDeliveryOrderDetailIn::getDeliveryQuantity, (v1, v2) -> v1));
////            zkGoodsQuantityMap = goodsDetailList.stream().collect(Collectors.toMap(HandleDeliveryOrderDetailIn::getGoodsCode, HandleDeliveryOrderDetailIn::getDeliveryQuantity, (s1, s2) -> s1));
//        }
//        Map<String, BigDecimal> finalZkGoodsQuantityMap = zkGoodsQuantityMap;
//        StringJoiner errorGoodsJoiner = new StringJoiner(SystemConstant.COMMA);
//        List<UpdateDisDeliveryDetailIn> updateDeliveryDetailInList = Lists.newArrayList();
//        disDeliveryDetailList.forEach(disDeliveryDetail -> {
//            String zkGoodsKey = this.getZkCallBackGoodsKey(disDeliveryDetail.getOtherGoodsCode(), disDeliveryDetail.getLine());
//            BigDecimal deliveryQuantity = finalZkGoodsQuantityMap.get(zkGoodsKey);
//            if (null == deliveryQuantity) {
//                zkGoodsKey = this.getZkCallBackGoodsKey(disDeliveryDetail.getOtherGoodsCode(), null);
//                deliveryQuantity = finalZkGoodsQuantityMap.get(zkGoodsKey);
//                deliveryQuantity = Objects.isNull(deliveryQuantity) ? BigDecimal.ZERO : deliveryQuantity;
//            }
//            // 过滤没有映射的
//            if (Objects.isNull(disDeliveryDetail.getOtherGoodsCode())) {
//                disDeliveryDetail.setDistributionQuantity(deliveryQuantity);
//                log.error("中科配销单{}发货回传商品{}没有映射", deliveryOrder.getDeliveryOrderNo(), disDeliveryDetail.getGoodsCode());
//            }
//            if (deliveryQuantity.compareTo(disDeliveryDetail.getDistributionQuantity()) == 1) {
//                errorGoodsJoiner.add(disDeliveryDetail.getGoodsCode());
//            }
//            UpdateDisDeliveryDetailIn updateDisDeliveryDetailIn = new UpdateDisDeliveryDetailIn();
//            updateDisDeliveryDetailIn.setId(disDeliveryDetail.getId());
//            updateDisDeliveryDetailIn.setDeliveryQuantity(deliveryQuantity);
//            updateDeliveryDetailInList.add(updateDisDeliveryDetailIn);
//        });
//        if (errorGoodsJoiner.length() > 0) {
//            String content = MessageFormat.format(DeliveryOrderLogEnum.ZK_AUDIT_DELIVERY_QUANTITY_ERROR.getKey(), deliveryOrder.getDeliveryOrderNo(), errorGoodsJoiner.toString());
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
//                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            return Response.error("中科回传实配数大于审核数");
//        }
//        deliveryOrder.setUpdater(SystemConstant.SYSTEM_USER);
//        deliveryOrder.setUpdateTime(handleDeliveryOrderIn.getHandleTime());
//        ordDisDeliveryService.updateDisDeliveryInfo(deliveryOrder, updateDeliveryDetailInList);
//        List<TakeDisDeliveryOrderGoodsIn> list = ordDisDeliveryDetailService.findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrder.getId());
//        TakeDisDeliveryOrderIn takeDisDeliveryOrderIn = new TakeDisDeliveryOrderIn();
//        takeDisDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
//        takeDisDeliveryOrderIn.setTakeDeliveryOrderGoodsInList(list);
//        takeDisDeliveryOrderIn.setTakeRemark("中科发货系统自动收货");
//        takeDisDeliveryOrderIn.setLoginUsername(SystemConstant.SYSTEM_USER);
//        takeDisDeliveryHandle.takeDelivery(takeDisDeliveryOrderIn, deliveryOrder, stockInfoOut);
//        return Response.success();
//    }
//
//    /**
//     * @param handleReturnOrderReceiveIn:
//     * @Description: 处理中科退货单收货回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/14 18:25
//     * @return: com.edc.plugins.common.response.Response<com.edc.erp.returnorder.entity.OrdDisReturn>
//     **/
//    @Transactional(rollbackFor = Exception.class)
//    public Response<String> zkReceiving(HandleReturnOrderReceiveIn handleReturnOrderReceiveIn) {
//        OrdDisReturn returnOrder = ordDisReturnService.getReturnOrderNoByIdAndBizOrgCode(handleReturnOrderReceiveIn.getDirectReturnNo(),
//                OrgCodeConvertEnum.getBizOrgCodeByOrgCode(handleReturnOrderReceiveIn.getOrgCode()));
//        if (Objects.isNull(returnOrder)) {
//            log.error("--->不存在的退货单{}", handleReturnOrderReceiveIn.getDirectReturnNo());
//            return Response.error("不存在的退货单");
//        }
//        if (!OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
//            log.error("退货单{}状态不正确", handleReturnOrderReceiveIn.getDirectReturnNo());
//            return Response.error("退货单状态不正确");
//        }
//        if (1 == handleReturnOrderReceiveIn.getAuditResult()) {
//            log.info("中科审核退货单{}未通过", handleReturnOrderReceiveIn.getDirectReturnNo());
//            return Response.error("中科审核退货单{}未通过");
//        }
//        if (CollectionUtils.isNotEmpty(handleReturnOrderReceiveIn.getDirectReturnDetailList())) {
//            Map<String, BigDecimal> zkReturnDetailMap = handleReturnOrderReceiveIn.getDirectReturnDetailList().stream()
//                    .collect(Collectors.toMap(handleReturnOrderDetailReceiveIn ->
//                            this.getZkCallBackGoodsKey(handleReturnOrderDetailReceiveIn.getGoodsCode(), handleReturnOrderDetailReceiveIn.getLine()), HandleReturnOrderDetailReceiveIn::getReturnQuantity, (v1, v2) -> v1));
////            Map<String, BigDecimal> zkReturnDetailMap = handleReturnOrderReceiveIn.getDirectReturnDetailList().stream()
////                    .collect(Collectors.toMap(HandleReturnOrderDetailReceiveIn::getGoodsCode, HandleReturnOrderDetailReceiveIn::getReturnQuantity));
//            List<OrdDisReturnDetail> dbReturnOrderDetails = ordDisReturnDetailService.findByReturnOrderId(returnOrder.getId());
//            StringJoiner errorGoodsJoiner = new StringJoiner(SystemConstant.COMMA);
//            List<OrdDisReturnDetail> needUpdateReturnOrderDetailList = Lists.newArrayList();
//            dbReturnOrderDetails.forEach(returnOrderDetail -> {
//                // 过滤没有映射的
//                if (StringUtils.isBlank(returnOrderDetail.getOtherGoodsCode())) {
//                    log.error("中科退货单{}商品{}没有映射", returnOrder.getReturnOrderNo(), returnOrderDetail.getGoodsCode());
//                    return;
//                }
//                String zkGoodsKey = this.getZkCallBackGoodsKey(returnOrderDetail.getOtherGoodsCode(), returnOrderDetail.getLine());
//                BigDecimal quantity = zkReturnDetailMap.get(zkGoodsKey);
//                if (null == quantity) {
//                    zkGoodsKey = this.getZkCallBackGoodsKey(returnOrderDetail.getOtherGoodsCode(), null);
//                    quantity = zkReturnDetailMap.get(zkGoodsKey);
//                    quantity = Objects.isNull(quantity) ? BigDecimal.ZERO : quantity;
//                }
//                if (quantity.compareTo(returnOrderDetail.getAuditReturnQuantity()) == 1) {
//                    errorGoodsJoiner.add(returnOrderDetail.getGoodsCode());
//                    return;
//                }
//                returnOrderDetail.setActualReturnQuantity(quantity);
//                returnOrderDetail.setUpdater(SystemConstant.SYSTEM_USER);
//                returnOrderDetail.setUpdateTime(LocalDateTime.now());
//                needUpdateReturnOrderDetailList.add(returnOrderDetail);
//            });
//            if (errorGoodsJoiner.length() > 0) {
//                String content = MessageFormat.format(ReturnOrderLogEnum.ZK_RETURN_ORDER_AUDIT_ACTUAL_ERROR.getKey(), returnOrder.getReturnOrderNo(), errorGoodsJoiner.toString());
//                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(returnOrder.getId()),
//                        OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), returnOrder.getCreator());
//                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                return Response.error("中科退货单收货回传发现实际退货数大于批准退货数");
//            }
//            if (CollectionUtils.isEmpty(needUpdateReturnOrderDetailList)) {
//                log.error("中科退货单{}没有可收货的商品", returnOrder.getReturnOrderNo());
//                return Response.error("中科退货单没有可收货的商品");
//            }
//            OrdSaveReturnOrderIn ordSaveReturnOrderIn = new OrdSaveReturnOrderIn();
//            ordSaveReturnOrderIn.setReturnOrderId(returnOrder.getId());
//            ordSaveReturnOrderIn.setReturnGoodsInfoInList(needUpdateReturnOrderDetailList);
//            ordSaveReturnOrderIn.setLoginUsername(SystemConstant.SYSTEM_USER);
//            return ordDisReturnService.receiving(ordSaveReturnOrderIn);
//        } else {
//            return Response.success("中科退货单明细为空");
//        }
//    }
//}
