package com.edc.erp.directly.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionRel;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionRelMapper;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yaojinpeng
 * @since 2022/10/30 23:39
 */
@Service
public class OrdDirOrderDistributionRelServiceImpl implements OrdDirOrderDistributionRelService {

    @Autowired
    private OrdDirOrderDistributionRelMapper distributionJoinOrderMapper;


    /**
     * 批量保存直营分货单与直营订货单关联表
     * @param distributionJoinOrderList 分货单与直营订货单关联集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDistributionJoinOrder(List<OrdDirOrderDistributionRel> distributionJoinOrderList) {
        int pages = distributionJoinOrderList.size() % SystemConstant.PAGE_SIZE == 0 ? distributionJoinOrderList.size() / SystemConstant.PAGE_SIZE : distributionJoinOrderList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            //批量保存
            distributionJoinOrderMapper.batchSaveDistributionJoinOrder(distributionJoinOrderList.subList(i * SystemConstant.PAGE_SIZE, i == pages -1 ? distributionJoinOrderList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }
}
