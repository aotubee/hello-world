package com.edc.erp;

import com.alibaba.fastjson.JSONObject;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/16 9:03
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disTestMq")
public class DisTestController {

//
//    @Qualifier("disBeforeCreateRequestOrderSender")
//    private final MessageSender disBeforeCreateRequestOrderSender;
//
//    @Qualifier("disAfterCreateRequestOrderSender")
//    private final MessageSender disAfterCreateRequestOrderSender;
//
//
//    @Qualifier("disFirstToDeliverySender")
//    private final MessageSender disFirstToDeliverySender;
//
//    @Qualifier("disDistributionCreateOrderSender")
//    private final MessageSender disDistributionCreateOrderSender;
//
//    @Qualifier("disDeliveryToDifferenceSender")
//    private final MessageSender disDeliveryToDifferenceSender;
//
//    @Qualifier("disDeliveryToDtsSender")
//    private final MessageSender disDeliveryToDtsSender;
//
//    @Qualifier("disDifferenceOrderToDtsSender")
//    private final MessageSender disDifferenceOrderToDtsSender;
//
//    @Qualifier("disReturnToDtsSender")
//    private final MessageSender disReturnToDtsSender;
//
//    @Qualifier("disDeliveryDtsToErpSender")
//    private final MessageSender disDeliveryDtsToErpSender;
//
//    @Qualifier("disReturnDtsToErpSender")
//    private final MessageSender disReturnDtsToErpSender;
//
//    @Qualifier("disDifferenceDtsToErpSender")
//    private final MessageSender disDifferenceDtsToErpSender;
//
//    @Qualifier("orderDisDeliveryDataFileSender")
//    private final MessageSender orderDisDeliveryDataFileSender;
//
//    @Qualifier("disPurchaseOrderToErpSender")
//    private final MessageSender disPurchaseOrderToErpSender;



    @Qualifier("zKWholesaleShipmentBackSender")
    private final MessageSender zKWholesaleShipmentBackSender;

    @Qualifier("zKWholesaleReturnBackSender")
    private final MessageSender zKWholesaleReturnBackSender;



    @PostMapping("/testMq")
    public Response<String> testMq(@RequestBody JSONObject jsonObject) {
        SendResponse sendResponse = null;
//        sendResponse = disBeforeCreateRequestOrderSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disAfterCreateRequestOrderSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disFirstToDeliverySender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDistributionCreateOrderSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDeliveryToDifferenceSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDeliveryToDtsSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDifferenceOrderToDtsSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disReturnToDtsSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDeliveryDtsToErpSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disReturnDtsToErpSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disDifferenceDtsToErpSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = orderDisDeliveryDataFileSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());
//        sendResponse = disPurchaseOrderToErpSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
//        log.info("-------------------->" + sendResponse.getMessageId());


        sendResponse = zKWholesaleShipmentBackSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
        log.info("-------------------->" + sendResponse.getMessageId());
        sendResponse = zKWholesaleReturnBackSender.sendSync(jsonObject.toJSONString().getBytes()); // PASS
        log.info("-------------------->" + sendResponse.getMessageId());
        return Response.success();
    }
}
