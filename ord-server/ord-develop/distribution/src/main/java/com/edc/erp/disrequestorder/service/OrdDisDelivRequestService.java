package com.edc.erp.disrequestorder.service;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderOut;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.model.out.AppOrderDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;



/**
 * 集货单(OrdDisDelivRequest)表服务接口
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
public interface OrdDisDelivRequestService extends BaseService<OrdDisDelivRequest> {
    /**
     * 运营端查集货单列表
     * @param backQueryRequestOrderPageIn
     * @return
     */
    Page<BackRequestOrderOut> findRequestOrderListForPage(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn);

    /**
     * 集货单单头信息
     * @param requestOrderId
     * @return
     */
    BackRequestOrderOut getRequestOrderById(Long requestOrderId);

    /**
     * 导出集货单列表
     * @param backQueryRequestOrderPageIn
     * @return
     */
    String export(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn);

    /**
     * 创建集货单
     *
     * @param orderCycle
     * @param legalOrderMap
     * @param itemCode
     * @return
     */
    OrdDisDelivRequest createRequestOrder(OrdDisOrderCycle orderCycle, Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap, String itemCode, Map<String, BigDecimal> illegalSkuAmountMap);

    /**
     * 保存集货单
     *
     * @param requestOrder
     */
    void saveRequestOrder(OrdDisDelivRequest requestOrder);

    /**
     * 根据集货单id查询集货单信息
     * @param requestOrderId
     * @return
     */
    OrdDisDelivRequest getRequestOrderByRequestOrderId(Long requestOrderId);

    /**
     * 根据配销单id、业务组织代码查询
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDisDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(Long id, String bizOrgCode);

    /**
     * 修改集货单状态
     *
     * @param id
     * @param requestStatusCode
     * @param systemUser
     * @param bizOrgCode
     */
    void updateRequestOrderStatus(Long id, String requestStatusCode, String systemUser, String bizOrgCode);

    /**
     * 查询时单段内未拆单的集货单号
     * @param createTimeBegin
     * @param createTimeEnd
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    /**
     * 集货单汇总
     * @param backQueryRequestOrderPageIn
     * @return
     */
    BigDecimal requestOrderSummary(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn);

    Long findOrderCycleIdByDeliveryOrderId(Long deliveryOrderId, String bizOrgCode);
}
