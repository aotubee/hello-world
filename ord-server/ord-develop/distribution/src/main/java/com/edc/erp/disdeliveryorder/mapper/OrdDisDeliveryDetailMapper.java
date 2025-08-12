package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.in.DisDeliveryOrderDetailsIn;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.WaitingForDisDeliveryGoodsIn;
import com.edc.erp.disdeliveryorder.model.out.DisDeliveryOrderArrivalDataOut;
import com.edc.erp.disdeliveryorder.model.out.DisDeliveryOrderDetailsOut;
import com.edc.erp.disdeliveryorder.model.out.DisDeliveryOrderDtlPrintOut;
import com.edc.erp.disdeliveryorder.model.out.DisOrderGoodsPreviewOut;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 配销单详情表(OrdDisDeliveryDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-10 19:48:21
 */
@Repository
public interface OrdDisDeliveryDetailMapper extends BaseMapper<OrdDisDeliveryDetail> {
    /**
     * 查询是否有明细
     *
     * @param deliveryOrderId
     * @return
     */
    OrdDisDeliveryDetail getDetail(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 查询配销明细列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    List<DisDeliveryOrderDetailsOut> findDeliveryOrderDetailsByPage(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn);

    /**
     * 批量添加配销单明细信息
     *
     * @param detailList
     * @return
     */
    int batchSave(@Param("detailList") List<OrdDisDeliveryDetail> detailList);

    /**
     * 查询仓位信息
     *
     * @param queryWarehouseIn
     * @return
     */
    List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn);

    /**
     * 根据code查询允许配货的仓位信息
     *
     * @param queryWarehouseIn
     * @return
     */
    StockInfoOut findStockInfoByCode(QueryWarehouseIn queryWarehouseIn);

    /**
     * 根据允许配货的仓位信息的仓储id查询仓储信息
     *
     * @param dbStockInfos
     * @return
     */
    List<WarehouseInfoOut> findWarehouseInfo(@Param("stockInfo") List<StockInfoOut> dbStockInfos, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询配销单明细集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    List<OrdDisDeliveryDetail> findDeliveryOrderDetailByDeliveryIds(@Param("ordDisDeliveryIds") List<Long> ordDisDeliveryIds);

    /**
     * 查询配销单明细集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(@Param("ordDisDeliveryIds") List<Long> ordDisDeliveryIds);

    /**
     * 批量更新发货量
     *
     * @param deliveryOrderDetails
     * @return
     */
    int updateBatchByDtsDtlList(@Param("updateDtlList") List<OrdDisDeliveryDetail> deliveryOrderDetails);


    /**
     * 根据配货单主键查询配货单明细集合
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<TakeDisDeliveryOrderGoodsIn> findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 根据配货单主键获取图片预览图
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<DisOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDisDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 待收货列表查询
     *
     * @param waitingForDeliveryGoodsIn 待收货配货单商品查询入参
     * @return
     */
    List<OrdDisDeliveryDetail> findListByTakeDisDelivery(WaitingForDisDeliveryGoodsIn waitingForDeliveryGoodsIn);


    /**
     * 批量修改配销单明细信息
     *
     * @param detailList
     * @return
     */
    int batchUpdate(@Param("ordDisDeliveryDetails") List<OrdDisDeliveryDetail> detailList);

    /**
     * 更新审核数信息
     *
     * @param disDeliveryDetailList
     * @return
     */
    int batchUpdateDistributionInfo(@Param("disDeliveryDetailList") List<OrdDisDeliveryDetail> disDeliveryDetailList);

    /**
     * 查询订单明细
     * @param ordDeliveryDetailIn
     * @return
     */
    List<OrdDisDeliveryDetail> findOrdDisDeliveryDetail(OrdDeliveryDetailIn ordDeliveryDetailIn);

    BigDecimal sumOrderAmount(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 发货后批量修改陪小单明细信息
     *
     * @param disDeliveryDetailList
     * @return
     */
    int batchUpdateDeliveryInfo(@Param("disDeliveryDetailList") List<OrdDisDeliveryDetail> disDeliveryDetailList);

    /**
     * 统计所有配销单明细实收数量和实收金额
     * @param bizOrgCode 业务组织
     * @return
     */
    OrdDisDeliveryDetail countTotalQuantityAndAmount(@Param("bizOrgCode") String bizOrgCode);

    /**
     * 发货后批量修改未占用到库存的成本值为0
     *
     * @param orderId
     * @return
     */
    void batchUpdateNotStock(@Param("deliveryOrderId") Long orderId, @Param("operator") String operator);

    int batchUpdateForSendZk(@Param("deliveryOrderDetailsList") List<OrdDisDeliveryDetail> deliveryOrderDetails);

    DisDeliveryOrderArrivalDataOut sumArrivalData(@Param("deliveryOrderId") Long deliveryOrderId);

    DisDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(@Param("deliveryOrderIdList") List<Long> deliveryOrderIdList);


    List<DisDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(@Param("deliveryOrderId") Long deliveryOrderId);

    List<OrdDisDeliveryDetail> findDetailSupplyRateList(@Param("deliveryOrderId") Long deliveryOrderId, @Param("storeCode") String storeCode,
                                                        @Param("orderCycleTime") LocalDateTime orderCycleTime);
}
