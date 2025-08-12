package com.edc.erp.distribution.service;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.model.out.OrdDisOverallDistributionDetailOut;
import com.edc.erp.distribution.model.out.OrdDisOverallDistributionStoreOut;
import com.edc.plugins.common.response.Response;

import java.util.List;

/**
 * @ClassName OrdDisOverallDistributionService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/18 11:49
 **/
public interface OrdDisOverallDistributionService {

    Response<Long> asyncImportOverallDistributionDetail(String fileId, OrdDisOrderDistribution ordDisOrderDistribution, String loginUsername, String distributionIdentification);

    OrdDisOverallDistributionDetailOut getOverallDistributionDetailList(OrdDisOrderDistribution ordDisOrderDistribution);
}
