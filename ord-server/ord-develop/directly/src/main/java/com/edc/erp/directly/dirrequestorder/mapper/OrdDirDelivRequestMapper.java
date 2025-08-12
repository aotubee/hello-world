package com.edc.erp.directly.dirrequestorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 要货单(OrdDirDelivRequest)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Repository
public interface OrdDirDelivRequestMapper extends BaseMapper<OrdDirDelivRequest> {
    /**
     * 运营端分页查询要货单列表
     *
     * @param dirRequestOrderPageIn
     * @return
     */
    List<DirRequestOrderOut> findRequestOrderListByPage(DirRequestOrderPageIn dirRequestOrderPageIn);

    /**
     * 根据要货单主键查询配货单号
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    String getDeliveryOrderNosById(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据要货单主键查询订货单号
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    String getOrderNosById(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据要货单主键查询表头信息
     *
     * @param requestOrderId
     * @return
     */
    DirRequestOrderOut getRequestOrderById(Long requestOrderId);

    /**
     * 根据配货单id查询要货单信息
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDirDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据要货单主键查询订货
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDirOrder> findOrderByRequestOrderId(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据要货单主键查询配货单
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDirDelivery> findDelivOrderByRequestOrderId(@Param("requestOrderId") Long requestOrderId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询时单段内未拆单的要货单号
     * @param createTimeBegin
     * @param createTimeEnd
     * @param statusCode
     * @param bizOrgCode
     * @return
     */
    List<String> findNotToDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin, @Param("createTimeEnd") String createTimeEnd,
                                            @Param("statusCode") String statusCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 要货单金额汇总
     * @param dirRequestOrderPageIn
     * @return
     */
    BigDecimal requestOrderSummary(DirRequestOrderPageIn dirRequestOrderPageIn);
}
