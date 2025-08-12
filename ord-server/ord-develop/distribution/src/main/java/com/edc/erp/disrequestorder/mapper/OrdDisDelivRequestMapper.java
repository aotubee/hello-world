package com.edc.erp.disrequestorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderOut;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 集货单(OrdDisDelivRequest)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-20 14:54:49
 */
@Repository
public interface OrdDisDelivRequestMapper extends BaseMapper<OrdDisDelivRequest> {
    /**
     * 运营端查集货单列表
     *
     * @param backQueryRequestOrderPageIn
     * @return
     */
    List<BackRequestOrderOut> findRequestOrderListByPage(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn);

    /**
     * 根据集货单主键查询配货单号
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    String getDeliveryOrderNosById(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据集货单主键查询订货单号
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    String getOrderNosById(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据集货单主键获取集货单单头信息
     *
     * @param requestOrderId
     * @return
     */

    BackRequestOrderOut getRequestOrderById(@Param("requestOrderId") Long requestOrderId);

    /**
     * 根据配销单id、业务组织代码查询集货单
     *
     * @param deliveryOrderId
     * @param bizOrgCode
     * @return
     */
    OrdDisDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(@Param("deliveryOrderId") Long deliveryOrderId,
                                                                     @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据集货单主键查询订货单
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisOrder> findOrderByRequestOrderId(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据集货单主键查询配销单
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisDelivery> findDelivOrderByRequestOrderId(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询时单段内未拆单的集货单号
     *
     * @param createTimeBegin
     * @param createTimeEnd
     * @param statusCode
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin, @Param("createTimeEnd") String createTimeEnd,
                                            @Param("statusCode") String statusCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 集货单金额汇总
     *
     * @param backQueryRequestOrderPageIn
     * @return
     */
    BigDecimal requestOrderSummary(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn);

    Long findOrderCycleIdByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId, @Param("bizOrgCode") String bizOrgCode);


}
