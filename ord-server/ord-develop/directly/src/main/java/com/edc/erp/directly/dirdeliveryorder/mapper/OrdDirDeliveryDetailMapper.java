package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirDeliveryOrderDetailsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.QueryWarehouseIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.TakeDirDeliveryOrderGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.WaitingForDirDeliveryGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 配货单详情表(OrdDirDeliveryDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-10 14:45:40
 */
@Repository
public interface OrdDirDeliveryDetailMapper extends BaseMapper<OrdDirDeliveryDetail> {
    /**
     *批处理保存
     * @param dirDeliveryDetails
     */
    void batchSave(@Param("dirDeliveryDetails") List<OrdDirDeliveryDetail> dirDeliveryDetails);

    /**
     * 根据允许配货的仓位信息的仓储id查询仓储信息
     * @param queryWarehouseIn
     * @return
     */
    StockInfoOut findStockInfoByCode(QueryWarehouseIn queryWarehouseIn);

    /**
     * 查询配货单是否有明细
     * @param deliveryOrderId
     * @return
     */
    OrdDirDeliveryDetail getDetail(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 查询配货单详情列表
     * @param deliveryOrderDetailsIn
     * @return
     */
    List<DirDeliveryOrderDetailsOut> findDeliveryOrderDetailsByPage(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn);

    /**
     * 查询配货单明细集合
     *
     * @param ordDirDeliveryIds
     * @return
     */
    List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(@Param("ordDirDeliveryIds") List<Long> ordDirDeliveryIds);

    /**
     * DTS回传更新明细
     * @param deliveryOrderDetails
     * @return
     */
    int updateBatchByDtsDtlList(@Param("updateDtlList") List<OrdDirDeliveryDetail> deliveryOrderDetails);

    /**
     * 根据配货单主键查询配货单明细集合
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<TakeDirDeliveryOrderGoodsIn> findTakeDirDeliveryOrderGoodsListByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 根据配货单主键获取图片预览图
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<DirOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);

    /**
     * 待收货列表查询
     *
     * @param waitingForDeliveryGoodsIn 待收货配货单商品查询入参
     * @return
     */
    List<OrdDirDeliveryDetail> findListByTakeDirDelivery(WaitingForDirDeliveryGoodsIn waitingForDeliveryGoodsIn);

    /**
     * 批量修改配货单明细信息
     *
     * @param detailList
     * @return
     */
    int batchUpdate(@Param("ordDirDeliveryDetails") List<OrdDirDeliveryDetail> detailList);

    /**
     * 查询订单明细
     * @param ordDeliveryDetailIn
     * @return
     */
    List<OrdDirDeliveryDetail> findOrdDirDeliveryDetail(OrdDeliveryDetailIn ordDeliveryDetailIn);

    /**
     * 更新审核数信息
     *
     * @param dirDeliveryDetailList
     * @return
     */
    int batchUpdateDistributionInfo(@Param("dirDeliveryDetailList") List<OrdDirDeliveryDetail> dirDeliveryDetailList);

    /**
     * 批量更新配货单明细信息
     * @param dirDeliveryDetailList
     * @return
     */
    int batchUpdateDeliveryInfo(@Param("disDeliveryDetailList") List<OrdDirDeliveryDetail> dirDeliveryDetailList);

    /**
     * 统计所有配货单明细实收数量和实收金额
     * @param bizOrgCode 业务组织
     * @return
     */
    OrdDirDeliveryDetail countTotalQuantityAndAmount(@Param("bizOrgCode") String bizOrgCode);

    /**
     * 发货后批量修改未占用到库存的成本值为0
     *
     * @param orderId
     * @return
     */
    void batchUpdateNotStock(@Param("deliveryOrderId") Long orderId, @Param("operator") String operator);

    DirDeliveryOrderArrivalDataOut sumArrivalData(@Param("deliveryOrderId") Long deliveryOrderId);

    DirDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(@Param("deliveryOrderIdList") List<Long> deliveryOrderIdList);

    List<DirDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(@Param("deliveryOrderId") Long deliveryOrderId);

    List<OrdDirDeliveryDetail> findDetailSupplyRateList(@Param("deliveryOrderId") Long deliveryOrderId, @Param("storeCode") String storeCode,
                                                        @Param("orderCycleTime") LocalDateTime orderCycleTime);

    List<DirDeliveryDetailListForReturnOut> findDetailListForReturnOrder(@Param("deliveryOrderId") Long deliveryOrderId);
}
