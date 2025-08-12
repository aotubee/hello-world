package com.edc.erp.ord.handle;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
import com.edc.sdk.dts.model.order.in.DifferenceBillIn;
import com.edc.sdk.dts.model.order.in.UnificationBillIn;
import com.edc.sdk.dts.model.order.in.UnificationReBillIn;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;
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
public class DirTaskRetryHandle {

    private final DirAsyncTaskItemService dirAsyncTaskItemService;
    private final OrdDirDeliveryService ordDirDeliveryService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final OrdDirDelivDifferenceService ordDirDelivDifferenceService;
    private final OrdDirReturnService ordDirReturnService;

    public void retryTask(AsyncTask asyncTask) {
        boolean resultFlag = false;
        String bizOrgCode = null;
        String businessNo = null;
        String messageStr = asyncTask.getMessageJson();
        if (AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS.equals(asyncTask.getType())) {
            UnificationBillIn unificationBillIn = JSON.parseObject(messageStr, UnificationBillIn.class);
            resultFlag = dirAsyncTaskItemService.dirDeliveryToDts(unificationBillIn);
            businessNo = unificationBillIn.getPlatform_bill_id();
            OrdDirDeliveryOut ordDirDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(businessNo);
            bizOrgCode = ordDirDeliveryOut.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIR_DIFFERENCE_ORDER_TO_DTS.equals(asyncTask.getType())) {
            DifferenceBillIn differenceBillIn = JSON.parseObject(messageStr, DifferenceBillIn.class);
            resultFlag = dirAsyncTaskItemService.dirDifferenceOrderToDts(differenceBillIn);
            businessNo = differenceBillIn.getPlatform_bill_id();
            OrdDirDelivDifference ordDirDelivDifference = ordDirDelivDifferenceService.getOneByOrderNo(businessNo);
            bizOrgCode = ordDirDelivDifference.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIR_RETURN_TO_DTS.equals(asyncTask.getType())) {
            UnificationReBillIn unificationReBillIn = JSON.parseObject(messageStr, UnificationReBillIn.class);
            resultFlag = dirAsyncTaskItemService.dirReturnToDts(unificationReBillIn);
            businessNo = unificationReBillIn.getPlatform_bill_id();
            OrdDirReturn ordDirReturn = ordDirReturnService.getReturnOrderByNo(businessNo);
            bizOrgCode = ordDirReturn.getBizOrgCode();
        }
        if (AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP.equals(asyncTask.getType())) {
            UnificationBillVO unificationBillVO = JSON.parseObject(messageStr, UnificationBillVO.class);
            bizOrgCode = unificationBillVO.getFdestorg();
            businessNo = unificationBillVO.getFsrcnum();
            resultFlag = dirAsyncTaskItemService.unificationOrderCallBack(unificationBillVO);
        }
        if (AsyncTaskConstant.Type.DIR_RETURN_DTS_TO_ERP.equals(asyncTask.getType())) {
            UnificationReBillVO unificationReBillVO = JSON.parseObject(messageStr, UnificationReBillVO.class);
            bizOrgCode = unificationReBillVO.getFdestorg();
            businessNo = unificationReBillVO.getFsrcnum();
            resultFlag = dirAsyncTaskItemService.unificationReOrderCallBack(unificationReBillVO);
        }
        if (AsyncTaskConstant.Type.DIR_DIFFERENCE_DTS_TO_ERP.equals(asyncTask.getType())) {
            DifferenceBillVO differenceBillVO = JSON.parseObject(messageStr, DifferenceBillVO.class);
            bizOrgCode = differenceBillVO.getFdestorg();
            businessNo = differenceBillVO.getFsrcnum();
            resultFlag = dirAsyncTaskItemService.differenceOrderCallBack(differenceBillVO);
        }
        if (AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_REQUEST.equals(asyncTask.getType())) {
            SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(messageStr, SendBeforeCreateRequestOrderMqIn.class);
            List<OrdDirOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
            bizOrgCode = orderList.get(0).getBizOrgCode();
            businessNo = orderList.get(0).getOrderNo();
            resultFlag = dirAsyncTaskItemService.dirDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
        }
        if (AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY.equals(asyncTask.getType())) {
            RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(messageStr, RequestOrderCreateMqIn.class);
            bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
            businessNo = requestOrderCreateMqIn.getRequestOrderNo();
            resultFlag = dirAsyncTaskItemService.dirRequestToDelivery(requestOrderCreateMqIn);
        }
        if (AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY.equals(asyncTask.getType())) {
            OrdDirOrderFirst ordDirOrderFirst = JSON.parseObject(messageStr, OrdDirOrderFirst.class);
            bizOrgCode = ordDirOrderFirst.getBizOrgCode();
            businessNo = ordDirOrderFirst.getFirstOrderNo();
            resultFlag = dirAsyncTaskItemService.dirFirstToDelivery(ordDirOrderFirst);
        }
        if (AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER.equals(asyncTask.getType())) {
            OrdDirOrderDistribution orderDistribution = JSON.parseObject(messageStr, OrdDirOrderDistribution.class);
            bizOrgCode = orderDistribution.getBizOrgCode();
            businessNo = orderDistribution.getDistributionOrderNo();
            resultFlag = dirAsyncTaskItemService.dirDistributionCreateOrder(orderDistribution);
        }
        if (AsyncTaskConstant.Type.DIR_DELIVERY_TO_DIFFERENCE.equals(asyncTask.getType())) {
            SaveDifferenceIn saveDifferenceIn = JSON.parseObject(messageStr, SaveDifferenceIn.class);
            bizOrgCode = saveDifferenceIn.getBizOrgCode();
            businessNo = saveDifferenceIn.getDeliveryOrderNo();
            resultFlag = dirAsyncTaskItemService.dirDeliveryToDifference(saveDifferenceIn);
        }
        if (AsyncTaskConstant.Type.DIR_PURCHASE_ORDER_TO_ERP.equals(asyncTask.getType())) {
            List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(messageStr, TransferNoticePurchaseVO.class);
            bizOrgCode = transferNoticePurchaseVOList.get(0).getBizOrgCode();
            businessNo = transferNoticePurchaseVOList.get(0).getPurchaseOrderNo();
            resultFlag = dirAsyncTaskItemService.dirPurchaseOrderToErp(transferNoticePurchaseVOList);
        }
        if (AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE.equals(asyncTask.getType())) {
            OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(messageStr, OrdDeliveryDataFile.class);
            bizOrgCode = ordDeliveryDataFile.getBizOrgCode();
            resultFlag = dirAsyncTaskItemService.execOrderDeliveryDataFile(ordDeliveryDataFile);
        }
        asyncPushTaskService.submitForMQ(asyncTask.getType(), messageStr, bizOrgCode, businessNo, resultFlag, null);
    }
}
