package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.common.model.in.warning.LogisticsMessageOrderQuantityIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.model.in.OrderDetailIn;
import com.edc.erp.directly.distribution.model.out.AppTopDirOrderGoodsOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailOut;
import com.edc.erp.directly.distribution.model.out.StoreOrderGoodsOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订货单详细表(OrdDirOrderDetail)表数据库访问层
 *
 * @author wanglidong
 * @since 2022-11-15 18:09:28
 */
@Repository
public interface OrdDirOrderDetailMapper extends BaseMapper<OrdDirOrderDetail> {

    /**
     * 查询直营订货单明细分页列表
     *
     * @param orderDetailIn
     * @return
     */
    List<OrderDetailOut> findOrderDetailOutByPage(@Param("orderDetailIn") OrderDetailIn orderDetailIn);

    BigDecimal sumExistsTotalPackageQuantityBySkuCodeAndOrderIdList(@Param("skuCode") String skuCode, @Param("orderIdList") List<Long> orderIdList);

    List<AppTopDirOrderGoodsOut> findTopOrderDetailListByOrderId(@Param("orderId") Long orderId, @Param("topNum") Integer topNum);

    /**
     * 查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量
     * @param orderCycleId
     * @param goodsCode
     * @return
     */
    BigDecimal getCycleOrderedGoodsQty(@Param("orderCycleId") Integer orderCycleId, @Param("goodsCode") String goodsCode);

    BigDecimal sumOrderSkuQuantity(LogisticsMessageOrderQuantityIn logisticsMessageOrderQuantityIn);

    void batchSaveDirOrderDetail(@Param("ordDirOrderDetailList") List<OrdDirOrderDetail> ordDirOrderDetailList);

    List<StoreOrderGoodsOut> findStoreOrderQuantityByTruncationTime(@Param("truncationTime") LocalDateTime truncationTime, @Param("storeCode") String storeCode);
}
