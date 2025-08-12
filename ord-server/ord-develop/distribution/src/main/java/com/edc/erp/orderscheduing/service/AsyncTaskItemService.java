package com.edc.erp.orderscheduing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleReturnSaveIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleShipmentSaveIn;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.sdk.dts.model.order.in.*;
import com.edc.sdk.dts.model.order.vo.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author fxw
 * @description: 异步任务接口
 * @since 2022/11/1 11:30
 */
public interface AsyncTaskItemService {

    /**
     * 任务接口关联
     * @throws Exception
     */
    void afterPropertiesSet() throws Exception;

    /**
     * 订货单生成集货单
     * @param sendBeforeCreateRequestOrderMqIn
     * @return
     */
    boolean disDistributionToRequest(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn);

    /**
     * 集货单转配销单
     *
     * @param requestOrderCreateMqIn
     * @return
     */
    boolean disRequestToDelivery(RequestOrderCreateMqIn requestOrderCreateMqIn);

    /**
     * 铺货单转配销单
     *
     * @param ordDisOrderFirst
     * @return
     */
    boolean disFirstToDelivery(OrdDisOrderFirst ordDisOrderFirst);

    /**
     * 配销单收货后生成配销差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    boolean disDeliveryToDifference(SaveDifferenceIn saveDifferenceIn);

    /**
     * 配销单下发DTS
     * @param unificationBillIn
     * @return
     */
    boolean disDeliveryToDts(UnificationBillIn unificationBillIn);

    /**
     * 配销差异单下发DTS
     *
     * @param differenceBillIn
     * @return
     */
    boolean disDifferenceOrderToDts(DifferenceBillIn differenceBillIn);

    /**
     * 配销退货单下发dts
     *
     * @param unificationReBillIn
     * @return
     */
    boolean disReturnToDts(UnificationReBillIn unificationReBillIn);

    /**
     * 批发出货单下发dts
     *
     * @param wholesaleBillIn
     * @return
     */
    boolean wholesaleShipmentToDts(WholesaleBillIn wholesaleBillIn);

    /**
     * 批发退货单下发dts
     *
     * @param wholesaleReBillIn
     * @return
     */
    boolean wholesaleReturnToDts(WholesaleReBillIn wholesaleReBillIn);

    /**
     * 采购订单回传配销单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    boolean disPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList);

//    /**
//     * 分货单生成订货单
//     * @param messageJson
//     * @return
//     */
//    String disDistributionToOrder(String messageJson);

    @Transactional(rollbackFor = Exception.class)
    boolean disDistributionCreateOrder(OrdDisOrderDistribution orderDistribution);

    /**
     * 配销单DTS回传
     * @param unificationBillVO
     * @return
     */
    boolean unificationOrderCallBack(UnificationBillVO unificationBillVO);

    /**
     * 配销退单DTS回传
     * @param unificationReBillVO
     * @return
     */
    boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO);

    /**
     * 配销差异单DTS回传
     * @param differenceBillVO
     * @return
     */
    boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO);

    /**
     * 批发退单DTS回传
     * @param wholesaleReBillVO
     * @return
     */
    boolean wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO);

    /**
     * 批发单DTS回传
     * @param wholesaleBillVO
     * @return
     */
    boolean wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO);

    /**
     * 配销单推送中科
     * @param messageJson
     * @return
     */
    String sendDeliveryOrderToZk(String messageJson);

    String sendReturnOrderToZk(String messageJson);

//    String zKAuditCallBack(String messageJson);

//    String zKConfirmCallBack(String messageJson);


    boolean zKCreateWholesaleShipment(TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn);

    boolean zKCreateWholesaleReturn(TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn);

    boolean zKWholesaleShipmentBack(WholesaleShipment wholesaleShipment);

    boolean zKWholesaleReturnBack(WholesaleReturns wholesaleReturns);

    boolean handleWholesaleShipmentPurchaseBack(List<TransferShipmentPushPurchaseBackVO> shipmentPushPurchaseBackVOList);

    boolean execOrderDeliveryDataFile(OrdDeliveryDataFile ordDeliveryDataFile);
}
