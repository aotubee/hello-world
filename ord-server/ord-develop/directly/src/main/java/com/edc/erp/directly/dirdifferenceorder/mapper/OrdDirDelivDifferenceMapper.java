package com.edc.erp.directly.dirdifferenceorder.mapper;

import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.model.in.DirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDiffOrderSummaryOut;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDifferenceOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 差异单(OrdDirDelivDifference)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-14 11:32:41
 */
@Repository
public interface OrdDirDelivDifferenceMapper extends BaseMapper<OrdDirDelivDifference> {
    /**
     * 分页获取差异单列表
     *
     * @param differenceOrderIn
     * @return
     */
    List<DirDifferenceOrderOut> findDifferenceOrdersByPage(DirDifferenceOrderIn differenceOrderIn);

    /**
     * 查询仓储库存
     *
     * @param bizOrgCode
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal checkStockInv(@Param("bizOrgCode") String bizOrgCode, @Param("goodsCode") String goodsCode, @Param("stockCode") String stockCode);

    /**
     * 查询门店可用库存
     *
     * @param bizOrgCode
     * @param goodsCode
     * @param storeCode
     * @return
     */
    BigDecimal checkStoreInv(@Param("bizOrgCode") String bizOrgCode, @Param("goodsCode") String goodsCode, @Param("storeCode") String storeCode);

    /**
     * 查询配货差异单列表（库存盘点）
     *
     * @param dirDelivDifference
     * @return
     */
    List<DirDifferenceOrderOut> findDirDifferenceOrders(OrdDirDelivDifference dirDelivDifference);

    /**
     * 通过配货单号查询差异单号
     *
     * @param deliveryOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDifferenceNoByDeliveryNo(@Param("deliveryOrderNo") String deliveryOrderNo, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 通过差异单号查询配货单号
     * @param differenceOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDeliveryNoByDiffNo(@Param("differenceOrderNo") String differenceOrderNo, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 直营差异单金额汇总
     * @param differenceOrderIn
     * @return
     */
    DirDiffOrderSummaryOut diffOrderSummary(DirDifferenceOrderIn differenceOrderIn);
}
