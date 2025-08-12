package com.edc.erp.ord.handle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleReturnSaveIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleShipmentSaveIn;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.sdk.dts.model.order.in.*;
import com.edc.sdk.dts.model.order.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName DisTaskRertyHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/7/16 9:42
 **/
@Service
@RequiredArgsConstructor
public class DisTaskRetryHandle {

    private final AsyncTaskItemService disAsyncTaskItemService;
    private final OrdDisDeliveryService ordDisDeliveryService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final OrdDisDelivDifferenceService ordDisDelivDifferenceService;
    private final OrdDisReturnService ordDisReturnService;
    private final WholesaleShipmentService wholesaleShipmentService;
    private final WholesaleReturnsService wholesaleReturnsService;

    public void retryTask(AsyncTask asyncTask) {
        boolean resultFlag = false;
        String bizOrgCode = null;
        String businessNo = null;
        String messageStr = asyncTask.getMessageJson();
        if (AsyncTaskConstant.Type.DIS_DELIVERY_TO_DTS.equals(asyncTask.getType())) {
            UnificationBillIn unificationBillIn = JSON.parseObject(messageStr, UnificationBillIn.class);
            resultFlag = disAsyncTaskItemService.disDeliveryToDts(unificationBillIn);
            businessNo = unificationBillIn.getPlatform_bill_id();
            OrdDisDeliveryOut ordDisDeliveryOut = ordDisDeliveryService.getDeliveryOrderOutByNo(businessNo);
            bizOrgCode = ordDisDeliveryOut.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIS_DIFFERENCE_ORDER_TO_DTS.equals(asyncTask.getType())) {
            DifferenceBillIn differenceBillIn = JSON.parseObject(messageStr, DifferenceBillIn.class);
            resultFlag = disAsyncTaskItemService.disDifferenceOrderToDts(differenceBillIn);
            businessNo = differenceBillIn.getPlatform_bill_id();
            OrdDisDelivDifference ordDisDelivDifference = ordDisDelivDifferenceService.getOneByOrderNo(businessNo);
            bizOrgCode = ordDisDelivDifference.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIS_RETURN_TO_DTS.equals(asyncTask.getType())) {
            UnificationReBillIn unificationReBillIn = JSON.parseObject(messageStr, UnificationReBillIn.class);
            resultFlag = disAsyncTaskItemService.disReturnToDts(unificationReBillIn);
            businessNo = unificationReBillIn.getPlatform_bill_id();
            OrdDisReturn ordDisReturn = ordDisReturnService.getReturnOrderByNo(businessNo);
            bizOrgCode = ordDisReturn.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS.equals(asyncTask.getType())) {
            WholesaleBillIn wholesaleBillIn = JSON.parseObject(messageStr, WholesaleBillIn.class);
            resultFlag = disAsyncTaskItemService.wholesaleShipmentToDts(wholesaleBillIn);
            businessNo = wholesaleBillIn.getPlatform_bill_id();
            WholesaleShipment wholesaleShipment = wholesaleShipmentService.getOneByNo(businessNo);
            bizOrgCode = wholesaleShipment.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS.equals(asyncTask.getType())) {
            WholesaleReBillIn wholesaleReBillIn = JSON.parseObject(messageStr, WholesaleReBillIn.class);
            resultFlag = disAsyncTaskItemService.wholesaleReturnToDts(wholesaleReBillIn);
            businessNo = wholesaleReBillIn.getPlatform_bill_id();
            WholesaleReturns wholesaleReturns = wholesaleReturnsService.getOneByOrderNo(businessNo);
            bizOrgCode = wholesaleReturns.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIS_DELIVERY_DTS_TO_ERP.equals(asyncTask.getType())) {
            UnificationBillVO unificationBillVO = JSON.parseObject(messageStr, UnificationBillVO.class);
            bizOrgCode = unificationBillVO.getFdestorg();
            businessNo = unificationBillVO.getFsrcnum();
            resultFlag = disAsyncTaskItemService.unificationOrderCallBack(unificationBillVO);
        }
        if (AsyncTaskConstant.Type.DIS_RETURN_DTS_TO_ERP.equals(asyncTask.getType())) {
            UnificationReBillVO unificationReBillVO = JSON.parseObject(messageStr, UnificationReBillVO.class);
            bizOrgCode = unificationReBillVO.getFdestorg();
            businessNo = unificationReBillVO.getFsrcnum();
            resultFlag = disAsyncTaskItemService.unificationReOrderCallBack(unificationReBillVO);
        }
        if (AsyncTaskConstant.Type.DIS_DIFFERENCE_DTS_TO_ERP.equals(asyncTask.getType())) {
            DifferenceBillVO differenceBillVO = JSON.parseObject(messageStr, DifferenceBillVO.class);
            bizOrgCode = differenceBillVO.getFdestorg();
            businessNo = differenceBillVO.getFsrcnum();
            resultFlag = disAsyncTaskItemService.differenceOrderCallBack(differenceBillVO);
        }
        if (AsyncTaskConstant.Type.DIS_WHOLESALE_DTS_TO_ERP.equals(asyncTask.getType())) {
            WholesaleBillVO wholesaleBillVO = JSON.parseObject(messageStr, WholesaleBillVO.class);
            bizOrgCode = wholesaleBillVO.getFdestorg();
            businessNo = wholesaleBillVO.getFsrcnum();
            resultFlag = disAsyncTaskItemService.wholesaleOrderCallBack(wholesaleBillVO);
        }
        if (AsyncTaskConstant.Type.DIS_WHOLESALE_RE_DTS_TO_ERP.equals(asyncTask.getType())) {
            WholesaleReBillVO wholesaleReBillVO = JSON.parseObject(messageStr, WholesaleReBillVO.class);
            bizOrgCode = wholesaleReBillVO.getFdestorg();
            businessNo = wholesaleReBillVO.getFsrcnum();
            resultFlag = disAsyncTaskItemService.wholesaleReOrderCallBack(wholesaleReBillVO);
        }
        if (AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_REQUEST.equals(asyncTask.getType())) {
            SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(messageStr, SendBeforeCreateRequestOrderMqIn.class);
            List<OrdDisOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
            bizOrgCode = orderList.get(0).getBizOrgCode();
            businessNo = orderList.get(0).getOrderNo();
            resultFlag = disAsyncTaskItemService.disDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
        }
        if (AsyncTaskConstant.Type.DIS_REQUEST_TO_DELIVERY.equals(asyncTask.getType())) {
            RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(messageStr, RequestOrderCreateMqIn.class);
            bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
            businessNo = requestOrderCreateMqIn.getRequestOrderNo();
            resultFlag = disAsyncTaskItemService.disRequestToDelivery(requestOrderCreateMqIn);
        }
        if (AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY.equals(asyncTask.getType())) {
            OrdDisOrderFirst ordDisOrderFirst = JSON.parseObject(messageStr, OrdDisOrderFirst.class);
            bizOrgCode = ordDisOrderFirst.getBizOrgCode();
            businessNo = ordDisOrderFirst.getFirstOrderNo();
            resultFlag = disAsyncTaskItemService.disFirstToDelivery(ordDisOrderFirst);
        }
        if (AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER.equals(asyncTask.getType())) {
            OrdDisOrderDistribution orderDistribution = JSON.parseObject(messageStr, OrdDisOrderDistribution.class);
            bizOrgCode = orderDistribution.getBizOrgCode();
            businessNo = orderDistribution.getDistributionOrderNo();
            // 分货单不能在这删除重复的key，因为多个门店独立创建订货单内部是try catch运行，如果重复消费，导致单店重复创建单据
            resultFlag = disAsyncTaskItemService.disDistributionCreateOrder(orderDistribution);
        }
        if (AsyncTaskConstant.Type.DIS_DELIVERY_TO_DIFFERENCE.equals(asyncTask.getType())) {
            SaveDifferenceIn saveDifferenceIn = JSON.parseObject(messageStr, SaveDifferenceIn.class);
            bizOrgCode = saveDifferenceIn.getBizOrgCode();
            businessNo = saveDifferenceIn.getDeliveryOrderNo();
            resultFlag = disAsyncTaskItemService.disDeliveryToDifference(saveDifferenceIn);
        }
        if (AsyncTaskConstant.Type.DIS_PURCHASE_ORDER_TO_ERP.equals(asyncTask.getType())) {
            List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(messageStr, TransferNoticePurchaseVO.class);
            bizOrgCode = transferNoticePurchaseVOList.get(0).getBizOrgCode();
            businessNo = transferNoticePurchaseVOList.get(0).getPurchaseOrderNo();
            resultFlag = disAsyncTaskItemService.disPurchaseOrderToErp(transferNoticePurchaseVOList);
        }
        if (AsyncTaskConstant.Type.ORDER_DIS_DELIVERY_DATA_FILE.equals(asyncTask.getType())) {
            OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(messageStr, OrdDeliveryDataFile.class);
            bizOrgCode = ordDeliveryDataFile.getBizOrgCode();
            resultFlag = disAsyncTaskItemService.execOrderDeliveryDataFile(ordDeliveryDataFile);
        }
        if (AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_PURCHASE_BACK.equals(asyncTask.getType())) {
            List<TransferShipmentPushPurchaseBackVO> shipmentPushPurchaseBackVOList = JSON.parseArray(messageStr, TransferShipmentPushPurchaseBackVO.class);
            bizOrgCode = shipmentPushPurchaseBackVOList.get(0).getBizOrgCode();
            businessNo = shipmentPushPurchaseBackVOList.get(0).getPurchaseOrderNo();
            resultFlag = disAsyncTaskItemService.handleWholesaleShipmentPurchaseBack(shipmentPushPurchaseBackVOList);
        }
        if (AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_SHIPMENT.equals(asyncTask.getType())) {
            TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn = JSON.parseObject(messageStr, TaskZKWholesaleShipmentSaveIn.class);
            bizOrgCode = taskZKWholesaleShipmentSaveIn.getBizOrgCode();
            businessNo = taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo();
            resultFlag = disAsyncTaskItemService.zKCreateWholesaleShipment(taskZKWholesaleShipmentSaveIn);
        }
        if (AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_RETURN.equals(asyncTask.getType())) {
            TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn = JSONObject.parseObject(messageStr, TaskZKWholesaleReturnSaveIn.class);
            bizOrgCode = taskZKWholesaleReturnSaveIn.getBizOrgCode();
            businessNo = taskZKWholesaleReturnSaveIn.getWholesaleReturnNo();
            resultFlag = disAsyncTaskItemService.zKCreateWholesaleReturn(taskZKWholesaleReturnSaveIn);
        }
        if (AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK.equals(asyncTask.getType())) {
            WholesaleShipment wholesaleShipment = JSONObject.parseObject(messageStr, WholesaleShipment.class);
            bizOrgCode = wholesaleShipment.getBizOrgCode();
            businessNo = wholesaleShipment.getShipmentNo();
            resultFlag = disAsyncTaskItemService.zKWholesaleShipmentBack(wholesaleShipment);
        }
        if (AsyncTaskConstant.Type.ZK_WHOLESALE_RETURN_BACK.equals(asyncTask.getType())) {
            WholesaleReturns wholesaleReturns = JSONObject.parseObject(messageStr, WholesaleReturns.class);
            bizOrgCode = wholesaleReturns.getBizOrgCode();
            businessNo = wholesaleReturns.getWholesaleReturnNo();
            resultFlag = disAsyncTaskItemService.zKWholesaleReturnBack(wholesaleReturns);
        }
        asyncPushTaskService.submitForMQ(asyncTask.getType(), messageStr, bizOrgCode, businessNo, resultFlag, null);
    }
}
