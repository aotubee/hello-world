package com.edc.erp.directly.distribution.service;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.model.out.OrdDirOverallDistributionDetailOut;
import com.edc.erp.directly.distribution.model.out.OrdDirOverallDistributionStoreOut;
import com.edc.plugins.common.response.Response;

import java.util.List;

/**
 * @ClassName OrdDisOverallDistributionService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/18 11:49
 **/
public interface OrdDirOverallDistributionService {

    Response<Long> asyncImportOverallDistributionDetail(String fileId, OrdDirOrderDistribution ordDirOrderDistribution, String loginUsername);

    OrdDirOverallDistributionDetailOut getOverallDistributionDetailList(OrdDirOrderDistribution ordDirOrderDistribution);
}
