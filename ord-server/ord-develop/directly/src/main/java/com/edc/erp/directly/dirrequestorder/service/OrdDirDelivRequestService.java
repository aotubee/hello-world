package com.edc.erp.directly.dirrequestorder.service;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.model.out.AppOrderDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 要货单(OrdDirDelivRequest)表服务接口
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
public interface OrdDirDelivRequestService extends BaseService<OrdDirDelivRequest> {
    /**
     * 运营端分页查询要货单列表
     *
     * @param dirRequestOrderPageIn
     * @return
     */
    Page<DirRequestOrderOut> findRequestOrderListForPage(DirRequestOrderPageIn dirRequestOrderPageIn);

    /**
     * 查询要货单明细表头
     *
     * @param requestOrderId
     * @return
     */
    DirRequestOrderOut getRequestOrderById(Long requestOrderId);

    /**
     * 运营端导出要货单列表
     *
     * @param dirRequestOrderPageIn
     * @return
     */
    String export(DirRequestOrderPageIn dirRequestOrderPageIn);

    /**
     * 根据要货单id查询要货单信息
     *
     * @param requestOrderId
     * @return
     */
    OrdDirDelivRequest getRequestOrderByRequestOrderId(Long requestOrderId);

    /**
     * 创建要货单
     *
     * @param orderCycle
     * @param legalOrderMap
     * @param itemCode
     * @return
     */
    OrdDirDelivRequest createRequestOrder(OrdDirOrderCycle orderCycle, Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, String itemCode);

    /**
     * 根据配货单id查询要货单信息
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDirDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(Long id, String bizOrgCode);

    /**
     * 修改要货单状态
     *
     * @param id
     * @param requestStatusCode
     * @param systemUser
     * @param bizOrgCode
     */
    void updateRequestOrderStatus(Long id, String requestStatusCode, String systemUser, String bizOrgCode);

    /**
     * 保存要货单
     *
     * @param requestOrder
     */
    void saveRequestOrder(OrdDirDelivRequest requestOrder);

    /**
     * 查询时单段内未拆单的集货单号
     * @param createTimeBegin
     * @param createTimeEnd
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    /**
     * 要货单汇总
     * @param dirRequestOrderPageIn
     * @return
     */
    BigDecimal requestOrderSummary(DirRequestOrderPageIn dirRequestOrderPageIn);
}
