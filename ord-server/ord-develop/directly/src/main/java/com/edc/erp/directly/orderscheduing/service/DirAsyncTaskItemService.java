package com.edc.erp.directly.orderscheduing.service;

import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.sdk.dts.model.order.in.DifferenceBillIn;
import com.edc.sdk.dts.model.order.in.UnificationBillIn;
import com.edc.sdk.dts.model.order.in.UnificationReBillIn;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;

import java.util.List;

/**
 * @author fxw
 * @description: 异步任务接口
 * @since 2022/11/1 11:30
 */
public interface DirAsyncTaskItemService {

    /**
     * 任务接口关联
     * @throws Exception
     */
    void afterPropertiesSet() throws Exception;

    /**
     * 订货单生成要货单
     * @param sendBeforeCreateRequestOrderMqIn
     * @return
     */
    boolean dirDistributionToRequest(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn);

    /**
     * 要货单转直营配货单
     *
     * @param requestOrderCreateMqIn
     * @return
     */
    boolean dirRequestToDelivery(RequestOrderCreateMqIn requestOrderCreateMqIn);

    /**
     * 铺货单转直营配货单
     *
     * @param ordDirOrderFirst
     * @return
     */
    boolean dirFirstToDelivery(OrdDirOrderFirst ordDirOrderFirst);

    /**
     * 直营配货单收货后生成直营配货差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    boolean dirDeliveryToDifference(SaveDifferenceIn saveDifferenceIn);

    /**
     * 直营配货单下发DTS
     * @param unificationBillIn
     * @return
     */
    boolean dirDeliveryToDts(UnificationBillIn unificationBillIn);

    /**
     * 直营配货差异单下发DTS
     *
     * @param differenceBillIn
     * @return
     */
    boolean dirDifferenceOrderToDts(DifferenceBillIn differenceBillIn);

    /**
     * 直营配货退货单下发dts
     *
     * @param unificationReBillIn
     * @return
     */
    boolean dirReturnToDts(UnificationReBillIn unificationReBillIn);

    /**
     * 批发出货单下发dts
     *
     * @param messageJson
     * @return
     */
    String wholesaleShipmentToDts(String messageJson);

    /**
     * 批发退货单下发dts
     *
     * @param messageJson
     * @return
     */
    String wholesaleReturnToDts(String messageJson);

    /**
     * 采购订单回传直营配货单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    boolean dirPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList);

//    /**
//     * 分货单生成订货单
//     * @param messageJson
//     * @return
//     */
//    String dirDistributionToOrder(String messageJson);

    /**
     * 配货单DTS回传
     * @param unificationBillVO
     * @return
     */
    boolean unificationOrderCallBack(UnificationBillVO unificationBillVO);

    /**
     * 退货单DTS回传
     * @param unificationReBillVO
     * @return
     */
    boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO);

    /**
     * 配货差异单DTS回传
     * @param differenceBillVO
     * @return
     */
    boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO);

//    /**
//     * 批发退单DTS回传
//     * @param messageJson
//     * @return
//     */
//    String wholesaleReOrderCallBack(String messageJson);

    /**
     * 批发单DTS回传
     * @param messageJson
     * @return
     */
    String wholesaleOrderCallBack(String messageJson);

    boolean execOrderDeliveryDataFile(OrdDeliveryDataFile ordDeliveryDataFile);

    boolean dirDistributionCreateOrder(OrdDirOrderDistribution ordDirOrderDistribution);
}


