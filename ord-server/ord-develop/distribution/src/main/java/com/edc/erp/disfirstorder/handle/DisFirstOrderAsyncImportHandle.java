package com.edc.erp.disfirstorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstMapper;
import com.edc.erp.disfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.disfirstorder.model.excel.OrdDisFirstOrderImportErrorResult;
import com.edc.erp.disfirstorder.model.out.DisFirstOrderCheckOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.plugins.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @ClassName DirFirstOrderAsyncImportHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/7 14:31
 **/
@Service
@AllArgsConstructor
@Slf4j
public class DisFirstOrderAsyncImportHandle {

    private final OrdDisOrderFirstMapper ordDisOrderFirstMapper;

    private final OrdDisOrderFirstDetailService ordDisOrderFirstDetailService;

    private final AsyncExportHandle asyncExportHandle;

    @Async
    public void handleAsyncFirstOrder(Long firstOrderId, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername) {
        OrdDisOrderFirst ordDisOrderFirst = null;
        try {
            ordDisOrderFirst = ordDisOrderFirstMapper.selectByPrimaryKey(firstOrderId);
            DisFirstOrderCheckOut disFirstOrderCheckOut = ordDisOrderFirstDetailService.handleCheckBeforeSaveFirstOrderDetail(ordDisOrderFirst, importFirstOrderDetailList, loginUsername);
            ordDisOrderFirstDetailService.saveImportFirstOrderDetail(disFirstOrderCheckOut);
            List<OrdDisFirstOrderImportErrorResult> errorResultList = disFirstOrderCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "配销铺货单【" + ordDisOrderFirst.getFirstOrderNo() + "】导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDisFirstOrderImportErrorResult.class);
        } catch (Exception e) {
            log.error("配销铺货单{}导入异常.", ordDisOrderFirst.getFirstOrderNo(), e);
            OrdDisFirstOrderImportErrorResult errorResult = new OrdDisFirstOrderImportErrorResult();
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("商品配置异常，请检查商品配置");
            String sheetName = "配销铺货单【" + ordDisOrderFirst.getFirstOrderNo() + "】导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisFirstOrderImportErrorResult.class);
        }
    }
}

