package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.distribution.model.out.OrdDisOrderDistributionDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 配销分货门店商品关联表(OrdDisOrderDistributionDetail)表数据库访问层
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Repository
public interface OrdDisOrderDistributionDetailMapper extends BaseMapper<OrdDisOrderDistributionDetail> {
    /**
     * 统计分货单总数量
     * @param distributionOrderId 配销分货单主键
     * @return
     */
    BigDecimal sumTotalDistributionQuantity(@Param("distributionOrderId") Long distributionOrderId);

    /**
     * 查询配销分货门店商品
     * @param queryOrderDistributionDetailIn 配销分货门店商品入参类
     * @return
     */
    List<OrdDisOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 批量保存明细集合
     * @param subList 明细集合
     */
    void batchSave(@Param("details") List<OrdDisOrderDistributionDetail> subList);

    void batchUpdateDistributionQuantity(@Param("detailList") List<OrdDisOrderDistributionDetail> detailList);

    void batchDelete(@Param("detailList") List<OrdDisOrderDistributionDetail> detailList);

}
