package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrdDisPresaleActivityStoreService extends BaseService<OrdDisPresaleActivityStore> {
    @Transactional(rollbackFor = Exception.class)
    void batchSavePresaleActivityStore(List<OrdDisPresaleActivityStore> ordDisPresaleActivityStoreList);

    void deleteByActivityId(Long activityId);

    List<OrdDisPresaleActivityStore> findStoreListByPresaleActivityId(Long activityId);
}
