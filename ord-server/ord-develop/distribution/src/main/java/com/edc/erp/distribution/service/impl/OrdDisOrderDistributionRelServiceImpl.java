package com.edc.erp.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionRel;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionRelMapper;
import com.edc.erp.distribution.service.OrdDisOrderDistributionRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yaojinpeng
 * @since 2022/10/30 23:39
 */
@Service
public class OrdDisOrderDistributionRelServiceImpl implements OrdDisOrderDistributionRelService {

    @Autowired
    private OrdDisOrderDistributionRelMapper distributionJoinOrderMapper;


    /**
     * 批量保存配销分货单与配销订货单关联表
     * @param distributionJoinOrderList 分货单与配销订货单关联集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDistributionJoinOrder(List<OrdDisOrderDistributionRel> distributionJoinOrderList) {
        int pages = distributionJoinOrderList.size() % SystemConstant.PAGE_SIZE == 0 ? distributionJoinOrderList.size() / SystemConstant.PAGE_SIZE : distributionJoinOrderList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            //批量保存
            distributionJoinOrderMapper.batchSaveDistributionJoinOrder(distributionJoinOrderList.subList(i * SystemConstant.PAGE_SIZE, i == pages -1 ? distributionJoinOrderList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }
}
