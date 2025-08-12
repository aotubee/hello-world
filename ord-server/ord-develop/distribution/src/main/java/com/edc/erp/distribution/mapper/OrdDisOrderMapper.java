package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.model.in.OrderIn;
import com.edc.erp.distribution.model.in.OrderQueryIn;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.distribution.model.out.DisOrderOut;
import com.edc.erp.distribution.model.out.OrderSummaryOut;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 配销订单表(OrdDisOrder)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Repository
public interface OrdDisOrderMapper extends BaseMapper<OrdDisOrder> {

    /**
     * 分页查询订货单列表
     *
     * @param orderIn
     * @return
     */
    List<DisOrderOut> findDisOrderByPage(OrderIn orderIn);

    /**
     * 根据id、业务组织代码获取订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    OrdDisOrder getOrderByIdAndBizOrgCode(@Param("orderId") Long orderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * @Description: 修改订单状态
     * @Author: ZhangYao
     * @Date: 2023/7/19 10:41
     * @param id:
     * @param bizOrgCode:
     * @param orderStatusCode:
     * @param updater:
     * @param freezeStatus:
     * @return: void
     **/
    void updateOrderStatus(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode, @Param("orderStatusCode") String orderStatusCode,
                           @Param("updater") String updater,  @Param("freezeStatus") String freezeStatus);

    /**
     * 根据集货单主键获取实付单价集合
     * @param requestOrderId
     * @return
     */
    List<BigDecimal> findRealUnitPrice(@Param("requestOrderId") Long requestOrderId);

    /**
     * 批量修改订单
     *
     * @param orderList
     */
    void batchUpdateOrder(@Param("orderList") List<OrdDisOrder> orderList);

    /**
     * 截单时间未支付状态修改
     *
     * @param order
     * @return
     */
    int updateForTruncationOrderByNoPay(OrdDisOrder order);

    /**
     * 批量修改集货单订货单关联关系
     *
     * @param orderList
     */
    void batchUpdateOrderRequestRel(@Param("orderList") List<OrdDisOrder> orderList);

    /**
     * 订货单汇总
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
    DisOrderOut getByIdAndOrg(@Param("orderId") Long orderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查找未作废的分货和跑货订单
     *
     * @param orderQueryIn
     * @return
     */
    List<OrdDisOrder> findOrderListByOrderQueryIn(OrderQueryIn orderQueryIn);

    List<OrdDisOrder> findNeedCutOrderListByParameter(OrderQueryIn orderQueryIn);

    /**
     * 门店上下限跑货订单金额超当天前30笔订单
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    List<BigDecimal> findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(@Param("storeCode")String storeCode,
                                                                                          @Param("orderTypeConfigId") Integer orderTypeConfigId,
                                                                                          @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件查询订货单
     *
     * @param targetOrder
     * @return
     */
    OrdDisOrder getLastUpAndDownOrder(OrdDisOrder targetOrder);

    BigDecimal sumOrderAmount(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode,
                              @Param("orderIdList") List<Long> orderIdList, @Param("orderStatusCode") String orderStatusCode);


    List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn);

    /**
     * 查询本截单时间未转单的订单号
     * @param truncationDateTime
     * @param orderStatusCodeList
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToRequestOrderList(@Param("truncationDateTime") String truncationDateTime, @Param("orderStatusCodeList") List<String> orderStatusCodeList, @Param("bizOrgCode") String bizOrgCode);

    void updateOrderSourceCode(@Param("orderIdList") List<Long> orderIdList, @Param("sourceCode") String sourceCode);

    void updateOrderOrderIdentification(@Param("orderIdList") List<Long> orderIdList, @Param("orderIdentification") String orderIdentification);

    List<OrdDisOrder> findNeedUnfreezeOrderNo(@Param("orderCycleId") Long orderCycleId);

    int batchReleaseOrderList(@Param("ordDisOrderList") List<OrdDisOrder> ordDisOrderList);
}
