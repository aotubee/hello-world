package com.edc.erp.directly.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionMapper;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.out.DirDistributionCheckOut;
import com.edc.erp.directly.distribution.model.out.OrdDirDistributionImportResultOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @ClassName DirDistributionOrderImportHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/1 1:11
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class DirDistributionOrderImportHandle {

    private final OrdDirOrderDistributionMapper ordDirOrderDistributionMapper;

    private final OrdDirOrderDistributionDetailService ordDirOrderDistributionDetailService;

    private final AsyncExportHandle asyncExportHandle;

    @Async
    public void handleAsyncDirDistribution(Map<String, OrdDirDistributionImportResultOut> importResultMap,
                                           Long distributionOrderId, String loginUsername, String type) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
        try {
            DirDistributionCheckOut dirDistributionCheckOut = ordDirOrderDistributionDetailService.handleCheckBeforeSaveDistributionDetail(importResultMap, ordDirOrderDistribution, loginUsername);
            ordDirOrderDistributionDetailService.saveImportDistributionDetail(dirDistributionCheckOut);
            List<OrdDirDistributionImportErrorResult> errorResultList = dirDistributionCheckOut.getErrorResultList();
//        List<OrdDirDistributionImportErrorResult> errorResultList = ordDirOrderDistributionDetailService.saveImportDistributionDetailCopy(importResultMap, ordDirOrderDistribution, loginUsername);
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "直营" + type + "分货单【" + ordDirOrderDistribution.getDistributionOrderNo() + "】导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDirDistributionImportErrorResult.class);
        } catch (Exception e) {
            log.error("直营{}分货单{}导入异常.", type, ordDirOrderDistribution.getDistributionOrderNo(), e);
            OrdDirDistributionImportErrorResult errorResult = new OrdDirDistributionImportErrorResult();
            errorResult.setStoreCode(SystemConstant.SHORT_LINE);
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("商品配置异常，请检查商品配置");
            String sheetName = "直营" + type + "分货单【" + ordDirOrderDistribution.getDistributionOrderNo() + "】导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirDistributionImportErrorResult.class);
        }
    }

//    private byte[] dirDistributionDetailDataToByte(String sheetName, List<OrdDirDistributionImportErrorResult> dataList) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ExcelWriter excelWriter = EasyExcelFactory.write(bos, OrdDirDistributionImportErrorResult.class).build();
//        WriteSheet writeSheet = EasyExcelFactory.writerSheet(NumberUtil.INTEGER_ONE).build();
//        writeSheet.setSheetName(sheetName);
//        excelWriter.write(dataList, writeSheet);
//        excelWriter.finish();
//        return bos.toByteArray();
//    }

}
