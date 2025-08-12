package com.edc.erp.directly.distribution.service;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionRel;

import java.util.List;

/**
 * @author yaojinpeng
 * @since 2022/10/30 23:38
 */
public interface OrdDirOrderDistributionRelService {

    /**
     * 批量保存直营分货单与直营订货单关联表
     * @param distributionJoinOrderList 分货单与直营订货单关联集合
     */
    void batchSaveDistributionJoinOrder(List<OrdDirOrderDistributionRel> distributionJoinOrderList);
}
