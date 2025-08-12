package com.edc.erp.disdifferenceorder.mapper;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.model.in.DisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.out.DisDiffOrderSummaryOut;
import com.edc.erp.disdifferenceorder.model.out.DisDifferenceOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 配销差异单(OrdDisDelivDifference)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-24 11:16:32
 */
@Repository
public interface OrdDisDelivDifferenceMapper extends BaseMapper<OrdDisDelivDifference> {
    /***
     * 获取差异单列表
     * @param differenceOrderIn
     * @return
     */
    List<DisDifferenceOrderOut> findDifferenceOrdersByPage(DisDifferenceOrderIn differenceOrderIn);

    /**
     * 查询门店可用库存
     * @param bizOrgCode
     * @param goodsCode
     * @param storeCode
     * @return
     */
    BigDecimal checkStoreInv(@Param("bizOrgCode") String bizOrgCode, @Param("goodsCode")String goodsCode,@Param("storeCode") String storeCode);

    /**
     * 查询仓储可用库存
     * @param bizOrgCode
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal checkStockInv(@Param("bizOrgCode") String bizOrgCode, @Param("goodsCode")String goodsCode,@Param("stockCode") String stockCode);

    /**
     * 查询配销差异单列表
     * @param disDelivDifference
     * @return
     */
    List<DisDifferenceOrderOut> findDisDifferenceOrders(OrdDisDelivDifference disDelivDifference);

    /**
     * 通过配销单号查询差异单号
     * @param deliveryOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDifferenceNoByDeliveryNo(@Param("deliveryOrderNo") String deliveryOrderNo, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 通过差异单号查询配销单号
     * @param differenceOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDeliveryNoByDiffNo(@Param("differenceOrderNo") String differenceOrderNo, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 加盟差异单金额汇总
     * @param differenceOrderIn
     * @return
     */
    DisDiffOrderSummaryOut diffOrderSummary(DisDifferenceOrderIn differenceOrderIn);
}
