package com.edc.erp.distribution.service;

import com.edc.erp.distribution.entity.OrdDisOrderDistributionRel;

import java.util.List;

/**
 * @author yaojinpeng
 * @since 2022/10/30 23:38
 */
public interface OrdDisOrderDistributionRelService {

    /**
     * 批量保存配销分货单与配销订货单关联表
     * @param distributionJoinOrderList 分货单与配销订货单关联集合
     */
    void batchSaveDistributionJoinOrder(List<OrdDisOrderDistributionRel> distributionJoinOrderList);
}
