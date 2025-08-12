package com.edc.erp.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.service.UnificationOrderService;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.sdk.dts.model.order.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * dts回传
 * @author lishaobo
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class UnificationOrderServiceImpl implements UnificationOrderService {

    private final AsyncPushTaskService asyncPushTaskService;

    @Qualifier("dirDeliveryDtsToErpSender")
    private final MessageSender dirDeliveryDtsToErpSender;

    @Qualifier("disDeliveryDtsToErpSender")
    private final MessageSender disDeliveryDtsToErpSender;

    @Qualifier("dirReturnDtsToErpSender")
    private final MessageSender dirReturnDtsToErpSender;

    @Qualifier("disReturnDtsToErpSender")
    private final MessageSender disReturnDtsToErpSender;

    @Qualifier("dirDifferenceDtsToErpSender")
    private final MessageSender dirDifferenceDtsToErpSender;

    @Qualifier("disDifferenceDtsToErpSender")
    private final MessageSender disDifferenceDtsToErpSender;

    @Qualifier("wholesaleOrderCallBackSender")
    private final MessageSender wholesaleOrderCallBackSender;

    @Qualifier("wholesaleReOrderCallBackSender")
    private final MessageSender wholesaleReOrderCallBackSender;



    @Override
    public Response<String> unificationOrderCallBack(UnificationBillVO unificationBillVO) {
//        String type = unificationBillVO.getFsrcnum().startsWith(OrderNoPreEnum.PH.getCode()) ? AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP : AsyncTaskConstant.Type.DIS_DELIVERY_DTS_TO_ERP;
//        asyncPushTaskService.submit(type, JSONObject.toJSONString(unificationBillVO), unificationBillVO.getFdestorg(), unificationBillVO.getFsrcnum());
        MessageSender messageSender = unificationBillVO.getFsrcnum().startsWith(OrderNoPreEnum.PH.getCode()) ? dirDeliveryDtsToErpSender : disDeliveryDtsToErpSender;
        SendResponse sendResponse = messageSender.sendSync(JSONObject.toJSONString(unificationBillVO).getBytes());
        log.info("DTS直营/加盟配货单数据回传{}数据回传消息ID---{}", unificationBillVO.getFsrcnum(), sendResponse.getMessageId());
        return Response.success();
    }

    @Override
    public Response<String> unificationReOrderCallBack(UnificationReBillVO unificationReBillVO) {
//        String type = unificationReBillVO.getFsrcnum().startsWith(OrderNoPreEnum.PT.getCode()) ? AsyncTaskConstant.Type.DIR_RETURN_DTS_TO_ERP : AsyncTaskConstant.Type.DIS_RETURN_DTS_TO_ERP;
//        asyncPushTaskService.submit(type, JSONObject.toJSONString(unificationReBillVO), unificationReBillVO.getFdestorg(), unificationReBillVO.getFsrcnum());
        MessageSender messageSender = unificationReBillVO.getFsrcnum().startsWith(OrderNoPreEnum.PT.getCode()) ? dirReturnDtsToErpSender : disReturnDtsToErpSender;
        SendResponse sendResponse = messageSender.sendSync(JSONObject.toJSONString(unificationReBillVO).getBytes());
        log.info("DTS直营/加盟退货单数据回传{}数据回传消息ID---{}", unificationReBillVO.getFsrcnum(), sendResponse.getMessageId());
        return Response.success();
    }

    @Override
    public Response<String> differenceOrderCallBack(DifferenceBillVO differenceBillVO) {
//        String type = differenceBillVO.getFsrcnum().startsWith(OrderNoPreEnum.KXC.getCode()) ? AsyncTaskConstant.Type.DIS_DIFFERENCE_DTS_TO_ERP : AsyncTaskConstant.Type.DIR_DIFFERENCE_DTS_TO_ERP;
//        asyncPushTaskService.submit(type, JSONObject.toJSONString(differenceBillVO), differenceBillVO.getFdestorg(), differenceBillVO.getFsrcnum());
        MessageSender messageSender = differenceBillVO.getFsrcnum().startsWith(OrderNoPreEnum.KXC.getCode()) ? disDifferenceDtsToErpSender : dirDifferenceDtsToErpSender;
        SendResponse sendResponse = messageSender.sendSync(JSONObject.toJSONString(differenceBillVO).getBytes());
        log.info("DTS直营/加盟差异单数据回传{}数据回传消息ID---{}", differenceBillVO.getFsrcnum(), sendResponse.getMessageId());
        return Response.success();
    }

    @Override
    public Response<String> wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO) {
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_WHOLESALE_RE_DTS_TO_ERP, JSONObject.toJSONString(wholesaleReBillVO), wholesaleReBillVO.getFdestorg(), wholesaleReBillVO.getFsrcnum());
        SendResponse sendResponse = wholesaleReOrderCallBackSender.sendSync(JSONObject.toJSONString(wholesaleReBillVO).getBytes());
        log.info("DTS批发退单{}数据回传消息ID---{}", wholesaleReBillVO.getFsrcnum(), sendResponse.getMessageId());
        return Response.success();
    }

    @Override
    public Response<String> wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO) {
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_WHOLESALE_DTS_TO_ERP, JSONObject.toJSONString(wholesaleBillVO), wholesaleBillVO.getFdestorg(), wholesaleBillVO.getFsrcnum());
        SendResponse sendResponse = wholesaleOrderCallBackSender.sendSync(JSONObject.toJSONString(wholesaleBillVO).getBytes());
        log.info("DTS批发单{}数据回传消息ID---{}", wholesaleBillVO.getFsrcnum(), sendResponse.getMessageId());
        return Response.success();
    }
}
