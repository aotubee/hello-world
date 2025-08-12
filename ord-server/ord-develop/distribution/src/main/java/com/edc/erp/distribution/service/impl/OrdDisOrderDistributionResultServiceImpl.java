package com.edc.erp.distribution.service.impl;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionResult;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionResultMapper;
import com.edc.erp.distribution.service.OrdDisOrderDistributionResultService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @ClassName OrdDisOrderDistributionResultServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/1 0:26
 **/
@Service
@RequiredArgsConstructor
public class OrdDisOrderDistributionResultServiceImpl extends BaseServiceImpl<OrdDisOrderDistributionResult> implements OrdDisOrderDistributionResultService {

    private final OrdDisOrderDistributionResultMapper ordDisOrderDistributionResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotExistOne(OrdDisOrderDistribution ordDirOrderDistribution, String storeCode) {
        OrdDisOrderDistributionResult checkResult = new OrdDisOrderDistributionResult();
        checkResult.setDistributionOrderId(ordDirOrderDistribution.getId());
        checkResult.setStoreCode(storeCode);
        checkResult.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
        checkResult.setIsDelete(ModelConst.DELETE.NO);
        checkResult = ordDisOrderDistributionResultMapper.selectOne(checkResult);
        if (Objects.isNull(checkResult)) {
            OrdDisOrderDistributionResult ordDisOrderDistributionResult = new OrdDisOrderDistributionResult();
            ordDisOrderDistributionResult.setDistributionOrderId(ordDirOrderDistribution.getId());
            ordDisOrderDistributionResult.setStoreCode(storeCode);
            ordDisOrderDistributionResult.setIsDone(ModelConst.DELETE.NO);
            ordDisOrderDistributionResult.setCreator(ordDirOrderDistribution.getUpdater());
            ordDisOrderDistributionResult.setCreateTime(LocalDateTime.now());
            ordDisOrderDistributionResult.setUpdater(ordDirOrderDistribution.getUpdater());
            ordDisOrderDistributionResult.setUpdateTime(LocalDateTime.now());
            ordDisOrderDistributionResult.setOrgCode(ordDirOrderDistribution.getOrgCode());
            ordDisOrderDistributionResult.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
            ordDisOrderDistributionResult.setIsDelete(ModelConst.DELETE.NO);
            ordDisOrderDistributionResultMapper.insert(ordDisOrderDistributionResult);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByDistributionOrderIdAndStoreCode(OrdDisOrderDistributionResult ordDisOrderDistributionResult) {
        ordDisOrderDistributionResultMapper.updateByDistributionOrderIdAndStoreCode(ordDisOrderDistributionResult);
    }
}
