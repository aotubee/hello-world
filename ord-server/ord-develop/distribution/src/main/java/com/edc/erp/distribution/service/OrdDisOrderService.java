package com.edc.erp.distribution.service;

import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.AppDisOrderCycleOut;
import com.edc.erp.distribution.model.out.DisOrderOut;
import com.edc.erp.distribution.model.out.OrderSummaryOut;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.log.dto.BusinessLog;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 配销订单表(OrdDisOrder)表服务接口
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
public interface OrdDisOrderService extends BaseService<OrdDisOrder> {

    /**
     * 分页查询配销订货单列表
     *
     * @param orderIn
     * @return
     */
    Page<DisOrderOut> findOrderOutPage(OrderIn orderIn);

    /**
     * 保存订货单
     *
     * @param ordDisOrder
     */
    void save(OrdDisOrder ordDisOrder);

    /**
     * 修改订货单
     *
     * @param ordDisOrder
     */
    void update(OrdDisOrder ordDisOrder);

    /**
     * 根据id、业务组织代码获取订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    OrdDisOrder getOrderByIdAndBizOrgCode(Long orderId, String bizOrgCode);

    /**
     * 修改订单状态
     *
     * @param id
     * @param bizOrgCode
     * @param orderStatusCode
     * @param systemUser
     */
    void updateOrderStatus(Long id, String bizOrgCode, String orderStatusCode, String systemUser, String freezeStatus);

    /**
     * 批量作废订货单
     *
     * @param batchInvalidOrderIn
     * @return
     */
    Response batchInvalidOrder(BatchInvalidOrderIn batchInvalidOrderIn);

    /**
     * 根据集货单主键获取实付单价
     *
     * @param requestOrderId
     * @return
     */
    BigDecimal getRealUnitPrice(Long requestOrderId);

    /**
     * 加推订货单
     *
     * @param ordDisOrder
     * @param userName
     * @return
     */
    Response<String> addPushOrder(OrdDisOrder ordDisOrder, String userName);

    /**
     * 根据截单时间批量作废
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    void batchInvalidForCutOrder(List<OrdDisOrder> orderList, String orderStatusCode, String updater);

    /**
     * 批量修改订单
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    List<BusinessLog> batchUpdateOrder(List<OrdDisOrder> orderList, String orderStatusCode, String updater);

    /**
     * 截单未支付
     *
     * @param order
     * @return
     */
    int updateForTruncationOrderByNoPay(OrdDisOrder order);

    /**
     * 批量修改集货单订货单关联关系
     *
     * @param orderList
     * @param id
     * @param systemUser
     */
    void batchUpdateOrderRequestRel(List<OrdDisOrder> orderList, Long id, String systemUser);

    /**
     * 根据集货单id查询关联的订货单列表
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisOrder> findOrderByRequestOrderIdAndBizOrgCode(Long requestOrderId, String bizOrgCode);

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
    DisOrderOut getOrderOutById(Long orderId, String bizOrgCode);

    /**
     * 导出订货单列表
     *
     * @param orderIn
     * @return
     */
    String exportOrderList(OrderIn orderIn);

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

    /**
     * 查找未下发ERP未作废的订货单
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisOrder> findNoInvalidOrderListByParameter(String storeCode, String truncationDateTime, Integer orderTypeId, String bizOrgCode);

    /**
     * 查找未作废的分货和跑货订单
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisOrder> findDistributionAndUpDownOrderList(String storeCode, String truncationDateTime, Integer orderTypeId, String bizOrgCode);

    /**
     * APP根据单号查询直营订货单
     *
     * @param orderNo
     * @param bizOrgCode
     * @return
     */
    DisOrderOut getOrderOut(String orderNo, String bizOrgCode);

    List<OrdDisOrder> findNeedCutOrderListByParameter(OrderQueryIn orderQueryIn);

    /**
     * 门店上下限跑货订单金额超当天前30笔订单
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param orgCode
     * @return
     */
    List<BigDecimal> findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(String storeCode, Integer orderTypeConfigId, String orgCode);

    /**
     * 校验订货清单商品相似度
     *
     * @param ordDisOrder
     * @param unitStr
     * @param errorMessage
     * @return
     */
    WarningResultOut checkOrderGoodsSimilarity(OrdDisOrder ordDisOrder, String unitStr, String errorMessage);

    /**
     * 根据条件查询订货单
     *
     * @param targetOrder
     * @return
     */
    OrdDisOrder getLastUpAndDownOrder(OrdDisOrder targetOrder);

    BigDecimal sumNeedPayAmount(String storeCode, String bizOrgCode, List<Long> orderIdList);

    BigDecimal sumPaidAmount(String storeCode, String bizOrgCode, List<Long> orderIdList);

    List<AppDisOrderCycleOut> findAppOrderList(AppQueryDisOrderIn appQueryOrderIn);

    int countDisOrderByOrderCycleId(Integer orderCycleId, String storeCode, String orderStatusCode);

    /**
     * 查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeConfigId
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getCycleOrderedGoodsQty(String storeCode, LocalDateTime truncationDateTime, Integer orderTypeConfigId, String goodsCode, String bizOrgCode);

    /**
     * 根据状态和订单类型ID及接单时间起止查询订单的门店代码集合
     *
     * @param queryPaidTransferOrderIn
     * @return
     */
    List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn);

    /**
     * 查询到点未接单的订单单号
     *
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToRequestOrderList(String truncationDateTime, String bizOrgCode);

    List<OrdDisOrder> findNeedUnfreezeOrderNo(Long orderCycleId);

    int batchReleaseOrderList(List<OrdDisOrder> ordDisOrderList);

    void updateOrderSourceCode(List<Long> orderIdList, String sourceCode);

    @Transactional(rollbackFor = Exception.class)
    void updateOrderOrderIdentification(List<Long> orderIdList, String orderIdentification);
}
