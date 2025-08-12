package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.excel.OrdDisDeliveryImportErrorResult;
import com.edc.erp.disdeliveryorder.model.in.DisDeliveryOrderDetailsIn;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.WaitingForDisDeliveryGoodsIn;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.UnificationBillDtlVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 配销单详情表(OrdDisDeliveryDetail)表服务接口
 *
 * @author weichao
 * @since 2022-10-10 19:48:14
 */
public interface OrdDisDeliveryDetailService extends BaseService<OrdDisDeliveryDetail> {
    /**
     * 查询是否有明细
     *
     * @param id
     * @return
     */
    OrdDisDeliveryDetail getDetail(Long id);

    /**
     * 查询配销明细列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    List<DisDeliveryOrderDetailsOut> findDeliveryOrderDetailsForPage(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn);

    /**
     * 保存配销单明细
     *
     * @param detailList
     * @param ordDisDelivery
     * @return
     */
    int save(List<OrdDisDeliveryDetail> detailList, OrdDisDelivery ordDisDelivery, String orderPriority);

    /**
     * 查询仓储与仓位信息(二级联动)
     *
     * @param bizOrgCode
     * @return
     */
    List<WarehouseInfoOut> findDisDeliveryStockInfo(String bizOrgCode);

    /**
     * 查询配销单明细集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    List<OrdDisDeliveryDetail> findDeliveryOrderDetailByDeliveryIds(List<Long> ordDisDeliveryIds);

    /**
     * 查询配销明细
     *
     * @param id
     * @return
     */
    List<OrdDisDeliveryDetail> findDeliveryOrderDetails(Long id);

    /**
     * 查询配销单明细集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(List<Long> ordDisDeliveryIds);

    /**
     * 查询允许某业务条件的仓位信息- 公共方法一定要传业务条件
     *
     * @param queryWarehouseIn
     * @return
     */
    List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn);

    /**
     * 获取仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    StockInfoOut findStockInfoByCode(String stockCode, String bizOrgCode);

    /**
     * 更新发货量
     *
     * @param deliveryOrder
     * @param detail
     * @return
     */
    int updateByDtsDtlList(OrdDisDelivery deliveryOrder, List<OrdDisDeliveryDetail> deliveryOrderDetails, List<UnificationBillDtlVO> detail,
                           Map<String, StandardGoodsInfoOut> standardGoodsMap, String centerStockBizOrgCode);


    /**
     * 根据配货单主键查询配货单明细集合
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<TakeDisDeliveryOrderGoodsIn> findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(Long deliveryOrderId);

    /**
     * 获取待收货商品列表
     *
     * @param waitingForDeliveryGoodsIn 待收货配货单商品查询入参
     * @return
     */
    WaitingForDisDeliveryInfoOut getTakeDisDeliveryInfoOut(WaitingForDisDeliveryGoodsIn waitingForDeliveryGoodsIn);

    /**
     * 根据配货单主键获取图片预览图
     *
     * @param deliveryOrderId 配货单主键
     * @return
     */
    List<DisOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDisDeliveryOrderId(Long deliveryOrderId);


    /**
     * 批量修改配销单明细信息
     *
     * @param detailList
     * @return
     */
    int batchUpdate(List<OrdDisDeliveryDetail> detailList);

    /**
     * 查询订单明细
     *
     * @param ordDeliveryDetailIn
     * @return
     */
    List<OrdDisDeliveryDetail> findOrdDisDeliveryDetail(OrdDeliveryDetailIn ordDeliveryDetailIn);

    /**
     * 更新审核数信息
     *
     * @param disDeliveryDetailList
     * @return
     */
    int batchUpdateDistributionInfo(List<OrdDisDeliveryDetail> disDeliveryDetailList);

    BigDecimal sumOrderAmount(Long deliveryOrderId);

    /**
     * 发货后批量修改陪小单明细信息
     *
     * @param detailList
     * @return
     */
    int batchUpdateDeliveryInfo(List<OrdDisDeliveryDetail> detailList);

    /**
     * 根据明细id
     *
     * @param id
     * @param deliveryOrderId
     * @return
     */
    OrdDisDeliveryDetail getOneByIdAndDeliveryOrderId(Long id, Long deliveryOrderId);

    /**
     * 统计所有配销单明细实收数量和实收金额
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    OrdDisDeliveryDetail countTotalQuantityAndAmount(String bizOrgCode);

    /**
     * 发货后批量修改未占用到库存的成本值为0
     *
     * @param orderId
     * @param operator
     * @return
     */
    void batchUpdateNotStock(Long orderId, String operator);

    void delete(OrdDisDeliveryDetail ordDisDeliveryDetail);

    int batchUpdateForSendZk(List<OrdDisDeliveryDetail> deliveryOrderDetails);

    DisDeliveryOrderArrivalDataOut sumArrivalData(Long deliveryOrderId);

    DisDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(List<Long> deliveryOrderIdList);

    List<DisDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(Long deliveryOrderId);

    List<OrdDisDeliveryDetail> findDetailSupplyRateList(Long deliveryOrderId, String storeCode, LocalDateTime orderCycleTime);
}
