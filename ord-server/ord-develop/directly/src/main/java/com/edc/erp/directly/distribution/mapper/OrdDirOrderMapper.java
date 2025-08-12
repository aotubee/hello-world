package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.common.model.in.warning.LogisticsMessageQueryIn;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.common.model.out.warning.LogisticsMessageOrderOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.model.in.DirOrderQueryIn;
import com.edc.erp.directly.distribution.model.in.OrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderOut;
import com.edc.erp.directly.distribution.model.out.OrderSummaryOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表(OrdDirOrder)表数据库访问层
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:16
 */
@Repository
public interface OrdDirOrderMapper extends BaseMapper<OrdDirOrder> {

    /**
     * 直营订货单分页查询
     *
     * @param orderIn
     * @return
     */
    List<DirOrderOut> findDirOrderByPage(OrderIn orderIn);

    /**
     * 根据id、业务组织代码获取直营订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    OrdDirOrder getOrderByIdAndBizOrgCode(@Param("orderId") Long orderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 修改直营订货单状态
     *
     * @param id
     * @param bizOrgCode
     * @param orderStatusCode
     * @param updater
     */
    void updateOrderStatus(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode, @Param("orderStatusCode") String orderStatusCode, @Param("updater") String updater);

    /**
     * 截单时间未支付状态修改
     *
     * @param order
     * @return
     */
    int updateForTruncationOrderByNoPay(OrdDirOrder order);

    /**
     * 批量修改订单
     *
     * @param orderList
     */
    void batchUpdateOrder(@Param("orderList") List<OrdDirOrder> orderList);


    /**
     * 批量修改要货单订货单关联关系
     *
     * @param orderList
     */
    void batchUpdateOrderRequestRel(@Param("orderList") List<OrdDirOrder> orderList);

    /**
     * 直营订货单汇总
     *
     * @param orderIn
     * @return
     */
    OrderSummaryOut orderSummary(OrderIn orderIn);

    /**
     * 根据id查询订货单出参信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    DirOrderOut getByIdAndOrg(@Param("orderId") Long orderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据要货单主键获取实付单价
     *
     * @param requestOrderId
     * @return
     */
    List<BigDecimal> findRealUnitPrice(@Param("requestOrderId") Long requestOrderId);


    List<OrdDirOrder> findNeedCutOrderListByParameter(DirOrderQueryIn dirOrderQueryIn);

    /**
     * 查找未作废的分货和跑货订单
     *
     * @param orderQueryIn
     * @return
     */
    List<OrdDirOrder> findOrderListByOrderQueryIn(DirOrderQueryIn orderQueryIn);

    /**
     * 门店上下限跑货订单金额超当天前30笔订单
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    List<BigDecimal> findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(@Param("storeCode") String storeCode, @Param("orderTypeConfigId") Integer orderTypeConfigId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件查询订货单
     *
     * @param targetOrder
     * @return
     */
    OrdDirOrder getLastUpAndDownOrder(OrdDirOrder targetOrder);

    BigDecimal sumOrderAmount(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode,
                              @Param("orderIdList") List<Long> orderIdList, @Param("orderStatusCode") String orderStatusCode);

    /**
     * 获取待付款已付款中转门店列表
     * @param queryPaidTransferOrderIn
     * @return
     */
    List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn);

    List<LogisticsMessageOrderOut> findOrderByTruncationDateTimeAndOrderStatus(LogisticsMessageQueryIn logisticsMessageQueryIn);

    /**
     * 查询本截单时间未转单的订单号
     * @param truncationDateTime
     * @param orderStatusCodeList
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToRequestOrderList(@Param("truncationDateTime") String truncationDateTime, @Param("orderStatusCodeList") List<String> orderStatusCodeList, @Param("bizOrgCode") String bizOrgCode);

    void updateOrderSourceCode(@Param("orderIdList") List<Long> orderIdList, @Param("sourceCode") String sourceCode);

    int getCountByParameter(@Param("storeCode") String storeCode, @Param("sourceCode") String sourceCode, @Param("truncationDateTime") LocalDateTime truncationDateTime);
}
