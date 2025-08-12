package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 直营分货门店商品关联表(OrdDisOrderDistributionDetail)表数据库访问层
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Repository
public interface OrdDirOrderDistributionDetailMapper extends BaseMapper<OrdDirOrderDistributionDetail> {
    /**
     * 统计分货单总数量
     * @param distributionOrderId 直营分货单主键
     * @return
     */
    BigDecimal sumTotalDistributionQuantity(@Param("distributionOrderId") Long distributionOrderId);

    /**
     * 查询直营分货门店商品
     * @param queryOrderDistributionDetailIn 直营分货门店商品入参类
     * @return
     */
    List<OrdDirOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 批量保存明细集合
     * @param subList 明细集合
     */
    void batchSave(@Param("details") List<OrdDirOrderDistributionDetail> subList);

    void batchUpdateDistributionQuantity(@Param("detailList") List<OrdDirOrderDistributionDetail> detailList);

    void batchDelete(@Param("detailList") List<OrdDirOrderDistributionDetail> detailList);
}
