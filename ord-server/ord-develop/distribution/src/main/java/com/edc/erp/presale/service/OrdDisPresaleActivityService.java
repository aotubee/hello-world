package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.model.excel.ImportPresaleActivityStore;
import com.edc.erp.presale.model.in.ExtendOrderDateIn;
import com.edc.erp.presale.model.in.PresaleActivityListPageIn;
import com.edc.erp.presale.model.in.SaveDisPresaleActivityIn;
import com.edc.erp.presale.model.out.*;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrdDisPresaleActivityService {
    Response<OrdDisPresaleActivity> savePresaleActivity(SaveDisPresaleActivityIn saveDisPresaleActivityIn);

    void beforeCheckSavePresaleActivityData(SaveDisPresaleActivityIn saveDisPresaleActivityIn);

    @Transactional(rollbackFor = Exception.class)
    Response<String> auditPresaleActivityData(SaveDisPresaleActivityIn saveDisPresaleActivityIn);

    List<Long> findNeedExecutePresaleActivityIdList();

    @Transactional(rollbackFor = Exception.class)
    void executedPresaleActivity(Long id, String loginUsername);

    OrdDisPresaleActivity getOneById(Long id);

    void invalidPresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity);

    @Transactional(rollbackFor = Exception.class)
    void stopPresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity);

    List<Long> findNeedStopPresaleActivityIdList();

    List<OrdDisPresaleActivity> findExecutePresaleActivityImageList(String storeCode);

    List<PresaleActivityInfoForAppOut> findPresaleActivityInfoListForApp(String storeCode);

    OrdDisPresaleActivity getPresaleActivityIsExecute(Long id);

    Page<PresaleActivityListOut> findPresaleActivityListByPage(PresaleActivityListPageIn presaleActivityListIn);

    PresaleActivityDetailOut findPresaleActivityById(Long id);

    @Transactional(rollbackFor = Exception.class)
    void terminatePresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity);

    Response<List<ImportPresaleActivityStoreOut>> importPresaleActivityStore(String fileId);

    Response<CheckExtendEndOrderDateOut> checkExtendEndOrderDate(ExtendOrderDateIn extendOrderDateIn, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    Response<String> extendEndOrderDate(CheckExtendEndOrderDateOut checkExtendEndOrderDateOut);
}
