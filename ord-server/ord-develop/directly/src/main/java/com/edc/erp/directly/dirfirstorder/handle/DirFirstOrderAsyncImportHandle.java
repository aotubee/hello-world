package com.edc.erp.directly.dirfirstorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstMapper;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.OrdDirFirstOrderImportErrorResult;
import com.edc.erp.directly.dirfirstorder.model.out.DirFirstOrderCheckOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDetailService;
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
public class DirFirstOrderAsyncImportHandle {

    private final OrdDirOrderFirstMapper ordDirOrderFirstMapper;

    private final OrdDirOrderFirstDetailService ordDirOrderFirstDetailService;

    private final AsyncExportHandle asyncExportHandle;

    @Async
    public void handleAsyncFirstOrder(Long firstOrderId, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername) {
        OrdDirOrderFirst ordDirOrderFirst = null;
        try {
            ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(firstOrderId);
            DirFirstOrderCheckOut dirDistributionCheckOut = ordDirOrderFirstDetailService.handleCheckBeforeSaveFirstOrderDetail(ordDirOrderFirst, importFirstOrderDetailList, loginUsername);
            ordDirOrderFirstDetailService.saveImportFirstOrderDetail(dirDistributionCheckOut);
            List<OrdDirFirstOrderImportErrorResult> errorResultList = dirDistributionCheckOut.getErrorResultList();
            if (CollectionUtils.isEmpty(errorResultList)) {
                return;
            }
            String sheetName = "直营铺货单【" + ordDirOrderFirst.getFirstOrderNo() + "】导入问题清单";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDirFirstOrderImportErrorResult.class);
        } catch (Exception e) {
            log.error("直营铺货单{}导入异常.", ordDirOrderFirst.getFirstOrderNo(), e);
            OrdDirFirstOrderImportErrorResult errorResult = new OrdDirFirstOrderImportErrorResult();
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("商品配置异常，请检查商品配置");
            String sheetName = "直营铺货单【" + ordDirOrderFirst.getFirstOrderNo() + "】导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirFirstOrderImportErrorResult.class);
        }
    }
}

