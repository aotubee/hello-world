package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.model.in.OrderDetailIn;
import com.edc.erp.distribution.model.out.AppTopDisOrderGoodsOut;
import com.edc.erp.distribution.model.out.OrderDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 配销订货单详细表(OrdDisOrderDetail)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Repository
public interface OrdDisOrderDetailMapper extends BaseMapper<OrdDisOrderDetail> {

    /**
     * 查询订货单明细分页列表
     *
     * @param orderDetailIn
     * @return
     */
    List<OrderDetailOut> findOrderDetailOutByPage(@Param("orderDetailIn") OrderDetailIn orderDetailIn);

    /**
     * 根据订单id查询包装数量之和
     *
     * @param skuCode
     * @param orderIdList
     * @return
     */
    BigDecimal sumExistsTotalPackageQuantityBySkuCodeAndOrderIdList(@Param("skuCode") String skuCode, @Param("orderIdList") List<Long> orderIdList);

    List<AppTopDisOrderGoodsOut> findTopOrderDetailListByOrderId(@Param("orderId") Long orderId, @Param("topNum") Integer topNum);

    /**
     * 查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量
     * @param orderCycleId
     * @param goodsCode
     * @return
     */
    BigDecimal getCycleOrderedGoodsQty(@Param("orderCycleId") Integer orderCycleId, @Param("goodsCode") String goodsCode);

    void batchSaveDisOrderDetail(@Param("ordDisOrderDetailList") List<OrdDisOrderDetail> orderDetailList);
}
