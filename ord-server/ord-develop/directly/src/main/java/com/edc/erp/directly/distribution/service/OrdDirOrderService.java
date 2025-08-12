package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.model.in.warning.LogisticsMessageQueryIn;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.common.model.out.warning.LogisticsMessageOrderOut;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.model.in.BatchInvalidOrderIn;
import com.edc.erp.directly.distribution.model.in.DirOrderQueryIn;
import com.edc.erp.directly.distribution.model.in.OrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailInfoOut;
import com.edc.erp.directly.distribution.model.out.OrderSummaryOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表(OrdDirOrder)表服务接口
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:12
 */
public interface OrdDirOrderService extends BaseService<OrdDirOrder> {

    /**
     * 根据订货单id查询订货单明细信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    OrderDetailInfoOut getOrderDetailInfoOut(Long orderId, String bizOrgCode);

    /**
     * 直营订货单分页查询
     *
     * @param orderIn
     * @return
     */
    Page<DirOrderOut> findOrderOutPage(OrderIn orderIn);

    /**
     * 根据id、业务组织代码获取直营订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    OrdDirOrder getOrderByIdAndBizOrgCode(Long orderId, String bizOrgCode);

    /**
     * 导出直营订货单
     *
     * @param orderIn
     * @return
     */
    String exportOrdDirOrder(OrderIn orderIn);

    /**
     * 修改直营订货单状态
     *
     * @param id
     * @param bizOrgCode
     * @param orderStatusCode
     * @param updater
     */
    void updateOrderStatus(Long id, String bizOrgCode, String orderStatusCode, String updater);

    /**
     * 截单未支付
     *
     * @param order
     * @return
     */
    int updateForTruncationOrderByNoPay(OrdDirOrder order);

    /**
     * 根据截单时间批量作废
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    void batchInvalidForCutOrder(List<OrdDirOrder> orderList, String orderStatusCode, String updater);

    /**
     * 批量修改订单
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    void batchUpdateOrder(List<OrdDirOrder> orderList, String orderStatusCode, String updater);

    /**
     * 修改订货单
     *
     * @param order
     */
    void update(OrdDirOrder order);

    /**
     * 保存订货单
     *
     * @param ordDisOrder
     */
    void save(OrdDirOrder ordDisOrder);

    /**
     * 批量修改要货单订货单关联关系
     *
     * @param orderList
     * @param id
     * @param systemUser
     */
    void batchUpdateOrderRequestRel(List<OrdDirOrder> orderList, Long id, String systemUser);

    /**
     * 根据要货单id查询关联的订货单列表
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDirOrder> findOrderByRequestOrderIdAndBizOrgCode(Long requestOrderId, String bizOrgCode);

    /**
     * 直营订货单汇总
     *
     * @param orderIn
     * @return
     */
    OrderSummaryOut orderSummary(OrderIn orderIn);

    /**
     * 批量作废直营订货单
     *
     * @param batchInvalidOrderIn
     * @return
     */
    Response batchInvalidOrder(BatchInvalidOrderIn batchInvalidOrderIn);

    /**
     * 根据id查询订货单出参信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    DirOrderOut getOrderOutById(Long orderId, String bizOrgCode);

    /**
     * 根据要货单主键获取实付单价
     *
     * @param requestOrderId
     * @return
     */
    BigDecimal getRealUnitPrice(Long requestOrderId);

    List<OrdDirOrder> findNeedCutOrderListByParameter(DirOrderQueryIn dirOrderQueryIn);

    /**
     * 门店上下限跑货订单金额超当天前30笔订单
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    List<BigDecimal> findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(String storeCode, Integer orderTypeConfigId, String bizOrgCode);

    /**
     * 校验订货清单商品相似度
     *
     * @param ordDirOrder
     * @param unitStr
     * @param errorMessage
     * @return
     */
    WarningResultOut checkOrderGoodsSimilarity(OrdDirOrder ordDirOrder, String unitStr, String errorMessage);

    /**
     * 根据条件查询订货单
     *
     * @param targetOrder
     * @return
     */
    OrdDirOrder getLastUpAndDownOrder(OrdDirOrder targetOrder);


    /**
     * 获取门店某个订货周期下指定订单类型下已付款跑货和分货金额
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    BigDecimal getTotalDistributionAndUpDownOrderAmount(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode);

    List<OrdDirOrder> findDistributionAndUpDownOrderList(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode);

    List<OrdDirOrder> findNoInvalidOrderListByParameter(String storeCode, String truncationDateTime, Integer orderTypeId, String bizOrgCode);

    BigDecimal sumOrderAmount(String storeCode, String bizOrgCode, List<Long> orderIdList);

    /**
     * 查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeConfigId
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getCycleOrderedGoodsQty(String storeCode, LocalDateTime truncationDateTime, Integer orderTypeConfigId, String goodsCode, String bizOrgCode);

    /**
     * 获取已提交中转门店列表
     * @param queryPaidTransferOrderIn
     * @return
     */
    List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn);

    List<LogisticsMessageOrderOut> findOrderByTruncationDateTimeAndOrderStatus(LogisticsMessageQueryIn logisticsMessageQueryIn);

    /**
     * 查询本截单时间未转单的订单号
     * @param truncationDateTime
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToRequestOrderList(String truncationDateTime, String bizOrgCode);

    @Transactional(rollbackFor = Exception.class)
    void updateOrderSourceCode(List<Long> orderIdList, String sourceCode);

    int getCountByParameter(String storeCode, String sourceCode, LocalDateTime truncationDateTime);
}
