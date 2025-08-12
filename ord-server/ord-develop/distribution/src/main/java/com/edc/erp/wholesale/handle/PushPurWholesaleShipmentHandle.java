package com.edc.erp.wholesale.handle;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.in.purchase.GoodsDtlsIn;
import com.edc.erp.common.model.in.purchase.TransferShipmentPushPurchaseIn;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.model.in.PushPurUpdateIn;
import com.edc.erp.wholesale.model.out.shipment.TransferShipmentPushPurchaseOut;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @ClassName PushPurWholesaleShipmentHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/1/22 11:54
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class PushPurWholesaleShipmentHandle {

    private final WholesaleShipmentService wholesaleShipmentService;

    private final PurchaseOrderClient purchaseOrderClient;

    private final AsyncLogService asyncLogService;

    public void handlePushToPur(String bizOrgCode, List<TransferShipmentPushPurchaseOut> wholesaleShipmentDetailList) {
        // 推送采购批次号
        String purBatchNumber = bizOrgCode + DateUtil.format(LocalDateTime.now(), "yyMMdd");
        Map<String, List<TransferShipmentPushPurchaseOut>> vendorMap = wholesaleShipmentDetailList.stream().collect(Collectors.groupingBy(detail ->
                detail.getVendorCode() + SystemConstant.SHORT_LINE + detail.getWarehouseCode() + SystemConstant.SHORT_LINE
                        + detail.getStockCode() + SystemConstant.SHORT_LINE + (StringUtils.isBlank(detail.getOrderPriority()) ? SystemConstant.COLON : detail.getOrderPriority())));
        AtomicReference<Integer> lineNo = new AtomicReference<>(NumberUtil.INTEGER_ONE);
        List<TransferShipmentPushPurchaseIn> resultList = vendorMap.entrySet().stream().map(entry -> {
            String[] keyArray = entry.getKey().split(SystemConstant.SHORT_LINE);
            String vendorCode = keyArray[0];
            String warehouseCode = keyArray[1];
            String stockCode = keyArray[2];
            String orderPriority = keyArray[3];
            Map<String, GoodsDtlsIn> goodsDtlsInMap = new HashMap<>();
            entry.getValue().forEach(transferShipmentPushPurchaseOut -> {
                GoodsDtlsIn goodsDtlsIn = goodsDtlsInMap.get(transferShipmentPushPurchaseOut.getGoodsCode());
                BigDecimal totalQty = new BigDecimal(transferShipmentPushPurchaseOut.getAuditQuantity());
                if (Objects.isNull(goodsDtlsIn)) {
                    goodsDtlsIn = new GoodsDtlsIn();
                    goodsDtlsIn.setGoodsCode(transferShipmentPushPurchaseOut.getGoodsCode());
//                        goodsDtlsIn.setLineNo(lineNo.get());
                    goodsDtlsIn.setLineNo(transferShipmentPushPurchaseOut.getLine());
                    goodsDtlsIn.setTotalQty(BigDecimal.ZERO);
                    lineNo.getAndSet(lineNo.get() + NumberUtil.INTEGER_ONE);
                }
                goodsDtlsIn.setTotalQty(goodsDtlsIn.getTotalQty().add(totalQty));
                goodsDtlsInMap.put(transferShipmentPushPurchaseOut.getGoodsCode(), goodsDtlsIn);
            });
            List<GoodsDtlsIn> goodsDtls = goodsDtlsInMap.values().stream().collect(Collectors.toList());
            TransferShipmentPushPurchaseIn transferShipmentPushPurchaseIn = new TransferShipmentPushPurchaseIn();
            transferShipmentPushPurchaseIn.setWarehouseCode(warehouseCode);
            transferShipmentPushPurchaseIn.setStockCode(stockCode);
            transferShipmentPushPurchaseIn.setVendorCode(vendorCode);
            transferShipmentPushPurchaseIn.setGoodsDtls(goodsDtls);
            transferShipmentPushPurchaseIn.setPurBatchNumber(purBatchNumber);
            transferShipmentPushPurchaseIn.setBizOrgCode(bizOrgCode);
            if (!SystemConstant.COLON.equals(orderPriority)) {
                transferShipmentPushPurchaseIn.setOrderPriority(orderPriority);
            }
            return transferShipmentPushPurchaseIn;
        }).collect(Collectors.toList());
        Response response = purchaseOrderClient.saveWholesaleShipmentOrder(resultList);
        log.info("推送采购返回---{}", response);
        if (!response.isSuccess()) {
            log.error("{}中转商品生成采购单失败" + (StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : null), bizOrgCode);
        } else {
            // 获取批发出id，更新批次号
            List<Long> shipmentIdList = wholesaleShipmentDetailList.stream().distinct().map(TransferShipmentPushPurchaseOut::getWholesaleShipmentId).collect(Collectors.toList());
            PushPurUpdateIn pushPurUpdateIn = new PushPurUpdateIn();
            pushPurUpdateIn.setShipmentIdList(shipmentIdList);
            pushPurUpdateIn.setPurBatchNumber(purBatchNumber);
            pushPurUpdateIn.setUpdater(SystemConstant.SYSTEM_USER);
            pushPurUpdateIn.setUpdateTime(LocalDateTime.now());
            pushPurUpdateIn.setPushPurProgress(PushPurProgressEnum.PUSHED.getProgress());
            wholesaleShipmentService.updatePurBatchNumberByIdList(pushPurUpdateIn);
            shipmentIdList.forEach(id -> {
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                        OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                        String.valueOf(id),
                        OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                        "推送至采购",
                        new Date(), SystemConstant.SYSTEM_USER);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            });
            log.info("中转批发出主键{}已成功推送采购", JSONArray.toJSONString(shipmentIdList));
        }
    }
}
