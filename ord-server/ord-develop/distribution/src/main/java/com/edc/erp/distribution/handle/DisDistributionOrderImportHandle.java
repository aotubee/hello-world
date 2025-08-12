package com.edc.erp.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionMapper;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.out.DisDistributionCheckOut;
import com.edc.erp.distribution.model.out.OrdDisDistributionImportResultOut;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @ClassName DirDistributionOrderImportHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/1 1:11
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class DisDistributionOrderImportHandle {

    private final OrdDisOrderDistributionMapper ordDisOrderDistributionMapper;

    private final OrdDisOrderDistributionDetailService ordDisOrderDistributionDetailService;

    private final AsyncExportHandle asyncExportHandle;

    @Async
    public void handleAsyncDirDistribution(Map<String, OrdDisDistributionImportResultOut> importResultMap, Long distributionOrderId,
                                           String loginUsername, String type, String distributionIdentification) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
        try {
            DisDistributionCheckOut dirDistributionCheckOut = ordDisOrderDistributionDetailService.handleCheckBeforeSaveDistributionDetail(importResultMap,
                    ordDisOrderDistribution, loginUsername, distributionIdentification);
            ordDisOrderDistributionDetailService.saveImportDistributionDetail(dirDistributionCheckOut);
            List<OrdDisDistributionImportErrorResult> errorResultList = dirDistributionCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "配销" + type + "分货单【" + ordDisOrderDistribution.getDistributionOrderNo() + "】导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDisDistributionImportErrorResult.class);
        } catch (Exception e) {
            log.error("配销{}分货单{}导入异常.", type, ordDisOrderDistribution.getDistributionOrderNo(), e);
            OrdDisDistributionImportErrorResult errorResult = new OrdDisDistributionImportErrorResult();
            errorResult.setStoreCode(SystemConstant.SHORT_LINE);
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("商品配置异常，请检查商品配置");
            String sheetName = "配销" + type + "分货单【" + ordDisOrderDistribution.getDistributionOrderNo() + "】导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisDistributionImportErrorResult.class);
        }
    }

}
