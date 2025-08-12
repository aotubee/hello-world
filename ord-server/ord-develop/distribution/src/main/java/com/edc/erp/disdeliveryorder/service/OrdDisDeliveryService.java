package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.purchase.FindTransferOrderIn;
import com.edc.erp.common.model.out.purchase.OrderDeliverRequestOut;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.in.*;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 配销单(OrdDisDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-10-10 19:48:29
 */
public interface OrdDisDeliveryService extends BaseService<OrdDisDelivery> {

    /**
     * 作废配销单
     *
     * @param deliveryOrderId
     * @param userName
     * @param isReturnAmount
     * @return
     */
    int invalidDisDeliverOrder(Long deliveryOrderId, String userName, boolean isReturnAmount);

    /**
     * 作废中装配销单
     *
     * @param delivery
     * @param deliveryDetails
     * @return
     */
    int invalidTransferOrder(OrdDisDelivery delivery, List<OrdDisDeliveryDetail> deliveryDetails);

    /**
     * @Description: 审核配销单
     * @Author: ZhangYao
     * @Date: 2025/5/6 18:38
     * @param stockInfoOut:
     * @param ordDisDelivery:
     * @return: com.edc.plugins.common.response.Response<com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut>
     **/
    Response<OrdDisDeliveryOut> audit(StockInfoOut stockInfoOut, OrdDisDelivery ordDisDelivery);

    /**
     * 分页查询配销单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    Page<DisDeliveryOrderOut> findDeliveryOrdersByPage(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * 配销单详情列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    Page<DisDeliveryOrderDetailsOut> findDeliveryOrderDetailForPage(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode);

    /**
     * 导出配销单明细
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    String exportDeliveryOrderDetails(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode);

    /**
     * 新增或修改配销单信息
     *
     * @param ordDisDeliveryIn
     * @return
     */
    Long saveOrUpdate(OrdDisDeliveryIn ordDisDeliveryIn);

    /**
     * 运营端查询配货单收货信息
     *
     * @param disDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    TakeDeliveryInfoOut getTakeDeliveryInfoByDeliveryOrderId(Long disDeliveryOrderId);

    /**
     * 批量新增配销单
     *
     * @param oddDisDelivers
     * @return
     */
    int batchInsert(List<OrdDisDelivery> oddDisDelivers);

    /**
     * 红冲配销单
     *
     * @param chargeDisDeliveryOrderIn
     * @param username
     * @return
     */
    Response chargeDeliveryOrder(ChargeDisDeliveryOrderIn chargeDisDeliveryOrderIn, String username);

    /**
     * 运营端查询配货单表头
     *
     * @param deliveryOrderId
     * @return
     */
    DisDeliveryOrderOut getDeliveryOrderOutByDeliveryOrderId(Long deliveryOrderId);

    /**
     * 校验商品信息
     *
     * @param deliveryIn
     * @return
     */
    OrdDisDeliveryDetailOut checkOrderGoods(DeliveryIn deliveryIn, String centerStockBizOrgCode);

    /**
     * 查询门店库存价
     *
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockPrice(String storeCode, String goodsCode, String bizOrgCode);

    /**
     * 查询门店库存价
     *
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockWarehousePrice(String wrhCode, String stockCode, String goodsCode, String bizOrgCode);

    /**
     * 运营端手动发货
     *
     * @param ordDisDelivery
     * @param updateDisDeliveryDetailInList
     */
    void updateDisDeliveryInfo(OrdDisDelivery ordDisDelivery, List<UpdateDisDeliveryDetailIn> updateDisDeliveryDetailInList, String loginBizOrgCode);


    /**
     * 发货调整库存
     *
     * @param ordDisDelivery
     * @param ordDisDeliveryDetails
     */
    void optInvForDeliveryInfo(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> ordDisDeliveryDetails, StockInfoOut stockInfoOut);

    /**
     * 发货返款
     *
     * @param ordDisDelivery
     * @param detailList
     */
    void shipmentsOrdDisDeliveryFund(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList);

    /**
     * 批量审核
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDisDeliveryIns
     * @return
     */
    Response bachAudit(String bizOrgCode, String loginUsername, List<OrdDisDeliveryIn> ordDisDeliveryIns);

    /**
     * 导出配销单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    String exportDeliveryOrder(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param deliveryOrderIn
     * @return
     */
    DisDeliveryOrderOut getDeliveryOrderTotal(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * 导入配销明细
     *
     * @param importDeliveryIn
     * @return
     */
    Response<List<DisDeliveryOrderDetailsOut>> importDeliveryDetail(ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode);

    /**
     * 查询配销单列表(库存盘点)
     *
     * @param deliveryOrderIn
     * @return
     */
    List<DisDeliveryOrderOut> findDisDeliveryOrder(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * 根据结转周期订货类型查询配销单信息
     *
     * @param findTransferOrderIn
     * @return
     */
    List<OrdDisDelivery> findTransferOrderByCarryForwardCycle(FindTransferOrderIn findTransferOrderIn);

    /**
     * 查询在单量
     *
     * @param storeCode
     * @param beginTime
     * @param endTime
     * @return
     */
    List<InOneQtyVO> findInDeliveryOrder(String storeCode, String beginTime, String endTime);

    /**
     * 重置指定收货
     *
     * @param id
     * @param bizOrgCode
     * @param loginUsername
     */
    void updateResetTakeDisDeliveryByDeliveryOrderId(Long id, String bizOrgCode, String loginUsername);

    List<OrdDisDelivery> findNeedAutoTakeDisDeliveryOrderList(@Param("nowTime") String nowTime, @Param("deliveryStatusCode") String deliveryStatusCode);

    int updateDisDeliveryTakeOngoingById(OrdDisDelivery deliveryOrder);

    /**
     * 采购订单回传配销单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    String disPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList);

    /**
     * 获取截单时间
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderDeliverRequestOut> findTruncationDateTime(String startTime, String endTime, String distributionType, String bizOrgCode);

    /**
     * 配销单操作库存
     *
     * @param detailList
     * @param operatorInvIns
     * @return
     */
    List<StockFlowIn> disDeliveryOrderOptInv(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, List<OperatorInvIn> operatorInvIns, StockInfoOut stockInfoOut);

    int countByStatusAndIdList(String deliveryOrderStatus, List<Long> idList);

    /**
     * 导出多个配销单明细
     *
     * @param deliveryOrderIn
     * @return
     */
    String exportDeliveryOrderDetailByOrder(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * @param idList:
     * @param bizOrgCode:
     * @Description: 查找未捞单的配销单
     * @Author: ZhangYao
     * @Date: 2023/3/13 17:33
     * @return: java.util.List<com.edc.erp.disdeliveryorder.model.out.NoSalvageDeliveryOrderOut>
     **/
    List<DisNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(List<Long> idList, String bizOrgCode);

    OrdDisDeliveryOut getDeliveryOrderOutByNo(String deliveryOrderNo);


    /**
     * 校验金额是否过大并提醒
     *
     * @param createTimeBegin
     * @param createTimeEnd
     * @param bizOrgCode
     */
    void checkDeliveryAmountSimilarity(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    List<DisNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    OrdDisDelivery getOneByDeliveryOrderNoAndBizOrgCode(String deliveryOrderNo, String bizOrgCode);

    List<DisDeliveryOrderPrintOut> findPrintDataByIds(List<Long> ids);

    Response<String> asyncImportDelivery(String fileId, String loginUsername, String loginBizOrgCode);

    @Transactional(rollbackFor = Exception.class)
    List<Long> saveAsyncImportDeliveryOrder(List<OrdDisDeliveryIn> ordDisDeliveryInList);

    @Transactional(rollbackFor = Exception.class)
    List<OrdDisDeliveryIn> initDeliveryOrderAndDetailForAsyncImport(List<ImportDisDeliveryOrder> deliveryOrderList, Map<String, StoreInfo> storeInfoMap,
                                                                    String loginUsername);

    Page<QueryDisDeliveryForReturnOut> findDeliveryOrderForReturn(DisDeliveryOrderIn deliveryOrderIn);

    List<DisStoreDeliveryNoInfoForAppOut> findStoreDeliveryInfoList(String storeCode);

//    Response<String> zkShippedToHd(OrdDisDelivery deliveryOrder,
//                                   List<HandleDeliveryOrderDetailIn> goodsDetailList,
//                                   LocalDateTime deliveryTime);

    List<OrdDisDelivery> findNeedFreezeDeliveryOrderList(String handleTime, String bizOrgCode);

    List<OrderConfigFreezeDeliveryOrderOut> findOrderConfigDeliveryOrderOut(List<OrdDisDelivery> disDeliveryList, String bizOrgCode);

    List<FirstOrderFreezeDeliveryOrderOut> findFirstOrderDeliveryOrderOut(List<OrdDisDelivery> disDeliveryList, String bizOrgCode);

    Integer countNoInvalidByCycleIdAndNonDeliveryOrderId(Long orderCycleId, Long deliveryOrderId, String bizOrgCode);

    Response payBeforeShipments(Long id, BigDecimal deliveryAmount);

    Integer countNoInvalidByFirstOrderAndNonDeliveryOrderId(Long firstOrderId, Long deliveryOrderId, String bizOrgCode);

    int batchUpdateFreeze(List<Long> idList, String loginUsername, String bizOrgCode);
}
