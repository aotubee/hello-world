package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirDeliveryOrderDetailsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.TakeDirDeliveryOrderGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.WaitingForDirDeliveryGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.UnificationBillDtlVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 配货单详情表(OrdDirDeliveryDetail)表服务接口
 *
 * @author weichao
 * @since 2022-11-10 14:45:40
 */
public interface OrdDirDeliveryDetailService extends BaseService<OrdDirDeliveryDetail> {

    /**
     * 保存配货单明细
     *
     * @param detailList
     * @param ordDirDelivery
     * @param orderPriority
     * @return
     */
    int save(List<OrdDirDeliveryDetail> detailList, OrdDirDelivery ordDirDelivery, String orderPriority);

    /**
     * 查询配货明细
     *
     * @param id
     * @return
     */
    List<OrdDirDeliveryDetail> findDeliveryOrderDetails(Long id);

    /**
     * 查询允许要货配货的仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    StockInfoOut findStockInfoByCode(String stockCode, String bizOrgCode);

    /**
     * 查询配货单是否有明细
     *
     * @param id
     * @return
     */
    OrdDirDeliveryDetail getDetail(Long id);

    /**
     * 查询配货单明细集合
     *
     * @param ordDirDeliveryIds
     * @return
     */
    List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(List<Long> ordDirDeliveryIds);

    /**
     * 查询配货单详情列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    List<DirDeliveryOrderDetailsOut> findDeliveryOrderDetailsForPage(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn);

    /**
     * DTS回传更新明细信息
     *
     * @param dirDelivery
     * @param ordDirDeliveryDetails
     * @param detail
     * @return
     */
    int updateByDtsDtlList(OrdDirDelivery dirDelivery, List<OrdDirDeliveryDetail> ordDirDeliveryDetails, List<UnificationBillDtlVO> detail, Map<String, StandardGoodsInfoOut> standardGoodsMap, String centerStockBizOrgCode);

    /**
     * 根据配货单主键查询配货单明细集合
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<TakeDirDeliveryOrderGoodsIn> findTakeDirDeliveryOrderGoodsListByDeliveryOrderId(Long deliveryOrderId);

    /**
     * 获取待收货商品列表
     *
     * @param waitingForDeliveryGoodsIn 待收货配货单商品查询入参
     * @return
     */
    WaitingForDirDeliveryInfoOut getTakeDirDeliveryInfoOut(WaitingForDirDeliveryGoodsIn waitingForDeliveryGoodsIn);

    /**
     * 根据配货单主键获取图片预览图
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<DirOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDisDeliveryOrderId(Long deliveryOrderId);


    /**
     * 批量修改配货单明细信息
     *
     * @param detailList
     * @return
     */
    int batchUpdate(List<OrdDirDeliveryDetail> detailList);

    /**
     * 查询订单明细
     *
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
    int batchUpdateDistributionInfo(List<OrdDirDeliveryDetail> dirDeliveryDetailList);

    /**
     * 批量更新配货单明细信息
     *
     * @param dirDeliveryDetailList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateDeliveryInfo(List<OrdDirDeliveryDetail> dirDeliveryDetailList);


    /**
     * 根据明细id和订货单id查询明细信息
     *
     * @param id
     * @param deliveryOrderId
     * @return
     */
    OrdDirDeliveryDetail getOneByIdAndDeliveryOrderId(Long id, Long deliveryOrderId);

    /**
     * 统计所有配货单明细实收数量和实收金额
     * @param bizOrgCode 业务组织
     * @return
     */
    OrdDirDeliveryDetail countTotalQuantityAndAmount(String bizOrgCode);

    void delete(OrdDirDeliveryDetail ordDirDeliveryDetail);

    DirDeliveryOrderArrivalDataOut sumArrivalData(Long deliveryOrderId);

    DirDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(List<Long> deliveryOrderIdList);

    List<DirDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(Long deliveryOrderId);

    List<OrdDirDeliveryDetail> findDetailSupplyRateList(Long deliveryOrderId, String storeCode, LocalDateTime orderCycleTime);

    List<DirDeliveryDetailListForReturnOut> findDetailListForReturnOrder(Long id);
}
