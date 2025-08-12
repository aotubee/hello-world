package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.common.model.in.purchase.FindTransferOrderIn;
import com.edc.erp.common.model.out.purchase.OrderDeliverRequestOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateOrdDirDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateResetTakeDirDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 配货单表(OrdDirDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-10 14:45:35
 */
@Repository
public interface OrdDirDeliveryMapper extends BaseMapper<OrdDirDelivery> {
    /**
     * 分页查询配货单信息
     *
     * @param deliveryOrderIn
     * @return
     */
    List<DirDeliveryOrderOut> findDeliveryOrdersByPage(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param deliveryOrderIn
     * @return
     */
    DirDeliveryOrderOut getDeliveryOrderTotal(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * 查询配货单列表(库存盘点)
     *
     * @param deliveryOrderIn
     * @return
     */
    List<DirDeliveryOrderOut> findDirDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * 根据结转周期订货类型查询配销单信息
     *
     * @param findTransferOrderIn
     * @return
     */
    List<OrdDirDelivery> findTransferOrderByCarryForwardCycle(FindTransferOrderIn findTransferOrderIn);

    /**
     * 根据配货单主键查询要货单
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDirDelivRequest getRequestOrder(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据订货周期主键查询订货周期
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    OrdDirOrderCycle getOrderCycleById(@Param("orderCycleId") Integer orderCycleId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据门店code查配销单
     *
     * @param storeCode
     * @param beginTime
     * @param endTime
     * @return
     */
    List<InOneQtyVO> findInDeliveryOrder(@Param("storeCode") String storeCode, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    void updateResetTakeDirDeliveryById(UpdateResetTakeDirDeliveryIn updateResetTakeDeliveryIn);

    List<OrdDirDelivery> findNeedAutoTakeDirDeliveryOrderList(@Param("nowTime") String nowTime, @Param("deliveryStatusCode") String deliveryStatusCode);

    int updateDirDeliveryTakeOngoingById(OrdDirDelivery deliveryOrder);

    /**
     * 修改配货单状态
     * @param updateOrdDirDeliveryIn
     */
    int updateOrdDirDeliveryStatus(UpdateOrdDirDeliveryIn updateOrdDirDeliveryIn);

    /**
     * 获取截单时间
     * @param startTime
     * @param endTime
     * @return
     */
    List<OrderDeliverRequestOut> findTruncationDateTime(@Param("startTime") String startTime,
                                                        @Param("endTime") String endTime,
                                                        @Param("distributionType") String distributionType,
                                                        @Param("bizOrgCode") String bizOrgCode);

    int countByStatusAndIdList(@Param("deliveryOrderStatus") String deliveryOrderStatus, @Param("idList") List<Long> idList);

    /**
     * 为惠之园导出配货单明细信息
     * @param deliveryOrderIn
     * @return
     */
    List<AsyncExcelDeliveryOrderDetail> findListForAsyncExportByPage(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * @Description: 查找未捞单的配货单
     * @Author: ZhangYao
     * @Date: 2023/3/13 19:05
     * @param idList:
     * @param deliveryStatusCode:
     * @param bizOrgCode:
     * @return: java.util.List<com.edc.erp.disdeliveryorder.model.out.NoSalvageDeliveryOrderOut>
     **/
    List<DirNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(@Param("idList") List<Long> idList, @Param("deliveryStatusCode") String deliveryStatusCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据时间段查询未审核的单据
     * @param createTimeBegin
     * @param createTimeEnd
     * @param orderStatusCodeList
     * @param bizOrgCode
     * @return
     */
    List<String> findNotAuditDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin, @Param("createTimeEnd") String createTimeEnd,
                                               @Param("orderStatusCodeList") List<String> orderStatusCodeList, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询当天前30笔配货单的金额
     * @param storeCode
     * @param bizOrgCode
     * @param stockCode
     * @param statusCode
     * @return
     */
    List<BigDecimal> findThirtyDaysDeliveryOrderTotalAmountByStoreCode(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode,
                                                                       @Param("stockCode") String stockCode, @Param("statusCode") String statusCode);



    List<DirNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(@Param("createTimeBegin") String createTimeBegin, @Param("createTimeEnd") String createTimeEnd,
                                                                      @Param("orderStatusCodeList") List<String> orderStatusCodeList, @Param("bizOrgCode") String bizOrgCode);


    SumDirDeliveryOrderDataOut sumDeliveryData(DirDeliveryOrderIn dirDeliveryOrderIn);

    List<Long> findSumDeliveryDataIdList(DirDeliveryOrderIn dirDeliveryOrderIn);


    List<DirDeliveryForReturnOut> findDeliveryOrderForReturnByPage(DirDeliveryOrderIn deliveryOrderIn);

    List<DirStoreDeliveryNoInfoForAppOut> findStoreDeliveryForAppReturnList(@Param("storeCode") String storeCode, @Param("minReceiveTime") LocalDateTime minReceiveTime);
}
