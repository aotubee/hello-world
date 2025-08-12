package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.common.model.in.purchase.FindTransferOrderIn;
import com.edc.erp.common.model.out.purchase.OrderDeliverRequestOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.model.in.DisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.in.UpdateOrdDisDeliveryIn;
import com.edc.erp.disdeliveryorder.model.in.UpdateResetTakeDisDeliveryIn;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 配销单(OrdDisDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-10 19:48:29
 */
@Repository
public interface OrdDisDeliveryMapper extends BaseMapper<OrdDisDelivery> {
    /**
     * 配销单分页
     *
     * @param deliveryOrderIn
     * @return
     */
    List<DisDeliveryOrderOut> findDeliveryOrdersByPage(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * 批量新增配销单
     *
     * @param ordDisDeliveries
     * @return
     */
    int batchInsert(@Param("ordDisDeliveries") List<OrdDisDelivery> ordDisDeliveries);

    /**
     * 查询门店库存价
     *
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockPrice(@Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询仓储库存价
     *
     * @param wrhCode
     * @param goodsCode
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockWarehousePrice(@Param("wrhCode") String wrhCode, @Param("stockCode") String stockCode, @Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询活动集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    List<OrdDisDelivery> findDeliveryOrdersByIds(@Param("ordDisDeliveryIds") List<Long> ordDisDeliveryIds);

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param disDeliveryOrderIn
     * @return
     */
    DisDeliveryOrderOut getDeliveryOrderTotal(DisDeliveryOrderIn disDeliveryOrderIn);

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
     * 根据配货单主键查询要货单
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDisDelivRequest getRequestOrder(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据订货周期主键查询订货周期
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    OrdDisOrderCycle getOrderCycleById(@Param("orderCycleId") Integer orderCycleId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据门店code查配销单
     *
     * @param storeCode
     * @param beginTime
     * @param endTime
     * @return
     */
    List<InOneQtyVO> findInDeliveryOrder(@Param("storeCode") String storeCode, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    void updateResetTakeDisDeliveryById(UpdateResetTakeDisDeliveryIn updateResetTakeDeliveryIn);

    List<OrdDisDelivery> findNeedAutoTakeDisDeliveryOrderList(@Param("nowTime") String nowTime, @Param("deliveryStatusCode") String deliveryStatusCode);

    int updateDisDeliveryTakeOngoingById(OrdDisDelivery deliveryOrder);

    /**
     * 修改配销单状态
     *
     * @param updateOrdDisDeliveryIn
     */
    int updateOrdDisDeliveryStatus(UpdateOrdDisDeliveryIn updateOrdDisDeliveryIn);

    /**
     * 获取截单时间
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderDeliverRequestOut> findTruncationDateTime(@Param("startTime") String startTime,
                                                        @Param("endTime") String endTime,
                                                        @Param("distributionType") String distributionType,
                                                        @Param("bizOrgCode") String bizOrgCode);

    int countByStatusAndIdList(@Param("deliveryOrderStatus") String deliveryOrderStatus, @Param("idList") List<Long> idList);

    /**
     * 为惠之园导出配销单明细信息
     *
     * @param deliveryOrderIn
     * @return
     */
    List<AsyncExcelDeliveryOrderDetail> findListForAsyncExportByPage(DisDeliveryOrderIn deliveryOrderIn);

    /**
     * @param idList:
     * @param deliveryStatusCode:
     * @param bizOrgCode:
     * @Description: 查找未捞单的配货单
     * @Author: ZhangYao
     * @Date: 2023/3/13 19:05
     * @return: java.util.List<com.edc.erp.disdeliveryorder.model.out.NoSalvageDeliveryOrderOut>
     **/
    List<DisNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(@Param("idList") List<Long> idList, @Param("deliveryStatusCode") String deliveryStatusCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据时间段查询未审核的单据
     *
     * @param createTimeBegin
     * @param createTimeEnd
     * @param orderStatusCodeList
     * @param bizOrgCode
     * @return
     */
    List<String> findNotAuditDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin, @Param("createTimeEnd") String createTimeEnd,
                                               @Param("orderStatusCodeList") List<String> orderStatusCodeList, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询当天前30笔配销单的金额
     *
     * @param storeCode
     * @param bizOrgCode
     * @param stockCode
     * @param statusCode
     * @return
     */
    List<BigDecimal> findThirtyDaysDeliveryOrderTotalAmountByStoreCode(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode,
                                                                       @Param("stockCode") String stockCode, @Param("statusCode") String statusCode);

    List<DisNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin,
                                                                      @Param("createTimeEnd") String createTimeEnd,
                                                                      @Param("orderStatusCodeList") List<String> orderStatusCodeList,
                                                                      @Param("bizOrgCode") String bizOrgCode);

    SumDisDeliveryOrderDataOut sumDeliveryData(DisDeliveryOrderIn disDeliveryOrderIn);

    List<Long> findSumDeliveryDataIdList(DisDeliveryOrderIn disDeliveryOrderIn);

    List<QueryDisDeliveryForReturnOut> findDeliveryOrderForReturnByPage(DisDeliveryOrderIn deliveryOrderIn);

    List<DisStoreDeliveryNoInfoForAppOut> findStoreDeliveryForAppReturnList(@Param("storeCode") String storeCode, @Param("minReceiveTime") LocalDateTime minReceiveTime);


    List<OrdDisDelivery> findNeedFreezeDeliveryOrderList(@Param("handleTime") String handleTime, @Param("bizOrgCode") String bizOrgCode);

    List<OrderConfigFreezeDeliveryOrderOut> findOrderConfigDeliveryOrderOut(@Param("disDeliveryList") List<OrdDisDelivery> disDeliveryList, @Param("bizOrgCode") String bizOrgCode);

    List<FirstOrderFreezeDeliveryOrderOut> findFirstOrderDeliveryOrderOut(@Param("disDeliveryList") List<OrdDisDelivery> disDeliveryList, @Param("bizOrgCode") String bizOrgCode);


    Integer countNoInvalidByCycleIdAndNonDeliveryOrderId(@Param("orderCycleId") Long orderCycleId,
                                                         @Param("deliveryOrderId") Long deliveryOrderId,
                                                         @Param("bizOrgCode") String bizOrgCode);

    Integer countNoInvalidByFirstOrderAndNonDeliveryOrderId(@Param("firstOrderId") Long firstOrderId,
                                                            @Param("deliveryOrderId") Long deliveryOrderId,
                                                            @Param("bizOrgCode") String bizOrgCode);

    int batchUpdateFreeze(@Param("idList") List<Long> idList, @Param("loginUsername") String loginUsername, @Param("bizOrgCode") String bizOrgCode);

}
