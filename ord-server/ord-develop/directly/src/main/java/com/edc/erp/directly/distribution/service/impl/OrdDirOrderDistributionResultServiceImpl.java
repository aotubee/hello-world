package com.edc.erp.directly.distribution.service.impl;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionResult;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionResultMapper;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionResultService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @ClassName OrdDirOrderDistributionResultServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/1 0:26
 **/
@Service
@RequiredArgsConstructor
public class OrdDirOrderDistributionResultServiceImpl extends BaseServiceImpl<OrdDirOrderDistributionResult> implements OrdDirOrderDistributionResultService {

    private final OrdDirOrderDistributionResultMapper ordDirOrderDistributionResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotExistOne(OrdDirOrderDistribution ordDirOrderDistribution, String storeCode) {
        OrdDirOrderDistributionResult checkResult = new OrdDirOrderDistributionResult();
        checkResult.setDistributionOrderId(ordDirOrderDistribution.getId());
        checkResult.setStoreCode(storeCode);
        checkResult.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
        checkResult.setIsDelete(ModelConst.DELETE.NO);
        checkResult = ordDirOrderDistributionResultMapper.selectOne(checkResult);
        if (Objects.isNull(checkResult)) {
            OrdDirOrderDistributionResult ordDirOrderDistributionResult = new OrdDirOrderDistributionResult();
            ordDirOrderDistributionResult.setDistributionOrderId(ordDirOrderDistribution.getId());
            ordDirOrderDistributionResult.setStoreCode(storeCode);
            ordDirOrderDistributionResult.setIsDone(ModelConst.DELETE.NO);
            ordDirOrderDistributionResult.setCreator(ordDirOrderDistribution.getUpdater());
            ordDirOrderDistributionResult.setCreateTime(LocalDateTime.now());
            ordDirOrderDistributionResult.setUpdater(ordDirOrderDistribution.getUpdater());
            ordDirOrderDistributionResult.setUpdateTime(LocalDateTime.now());
            ordDirOrderDistributionResult.setOrgCode(ordDirOrderDistribution.getOrgCode());
            ordDirOrderDistributionResult.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
            ordDirOrderDistributionResult.setIsDelete(ModelConst.DELETE.NO);
            ordDirOrderDistributionResultMapper.insert(ordDirOrderDistributionResult);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByDistributionOrderIdAndStoreCode(OrdDirOrderDistributionResult ordDirOrderDistributionResult) {
        ordDirOrderDistributionResultMapper.updateByDistributionOrderIdAndStoreCode(ordDirOrderDistributionResult);
    }
}
