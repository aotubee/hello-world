package com.edc.erp.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author fxw
 * @description: 采购订单信息回传erp系统
 * @since 2022/11/4 12:04
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/purchaseCallBack")
@Api(value = "采购订单回传erp", tags = "采购订单回传erp")
public class PurchaseCallBackController {

    private final AsyncPushTaskService asyncPushTaskService;

    @Qualifier("disPurchaseOrderToErpSender")
    private final MessageSender disPurchaseOrderToErpSender;


    @Qualifier("dirPurchaseOrderToErpSender")
    private final MessageSender dirPurchaseOrderToErpSender;

    @Qualifier("handleWholesaleShipmentPurchaseBackSender")
    private final MessageSender handleWholesaleShipmentPurchaseBackSender;

    /**
     * 中转商品生成采购订单回传erp系统
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    @ApiOperation(value = "中转商品生成采购订单回传erp系统", notes = "中转商品生成采购订单回传erp系统")
    @PostMapping("/purchaseOrderToErp")
    public Response<String> purchaseOrderToErp(@RequestBody @Valid List<TransferNoticePurchaseVO> transferNoticePurchaseVOList) {
//        log.info(JSONArray.toJSONString(transferNoticePurchaseVOList));
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_PURCHASE_ORDER_TO_ERP, JSONArray.toJSONString(transferNoticePurchaseVOList), transferNoticePurchaseVOList.get(0).getBizOrgCode(), transferNoticePurchaseVOList.get(0).getPurchaseOrderNo());
        SendResponse disSendResponse = disPurchaseOrderToErpSender.sendSync(JSONArray.toJSONString(transferNoticePurchaseVOList).getBytes());
        log.info("采购订单回传配销单消息ID---{}", disSendResponse.getMessageId());
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_PURCHASE_ORDER_TO_ERP, JSONArray.toJSONString(transferNoticePurchaseVOList), transferNoticePurchaseVOList.get(0).getBizOrgCode(), transferNoticePurchaseVOList.get(0).getPurchaseOrderNo());
        SendResponse dirSendResponse = dirPurchaseOrderToErpSender.sendSync(JSONArray.toJSONString(transferNoticePurchaseVOList).getBytes());
        log.info("采购订单回传配货单消息ID---{}", dirSendResponse.getMessageId());
        return Response.success();
    }

    @ApiOperation(value = "批发出中转商品生成采购订单回传erp采购单号", notes = "中转商品生成采购订单回传erp系统采购单号")
    @PostMapping("/wholesaleShipmentPurchaseBack")
    public Response<String> wholesaleShipmentPurchaseBack(@RequestBody @Valid List<TransferShipmentPushPurchaseBackVO> transferShipmentPushPurchaseBackVOS) {
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_PURCHASE_BACK, JSON.toJSONString(transferShipmentPushPurchaseBackVOS),
//                transferShipmentPushPurchaseBackVOS.get(0).getBizOrgCode(), transferShipmentPushPurchaseBackVOS.get(0).getPurchaseOrderNo());
        SendResponse sendResponse = handleWholesaleShipmentPurchaseBackSender.sendSync(JSON.toJSONString(transferShipmentPushPurchaseBackVOS).getBytes());
        log.info("批发中转商品发采购回传采购单号消息ID---{}", sendResponse.getMessageId());
        return Response.success();
    }
}
