package com.edc.erp.presale.service.impl;

import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import com.edc.erp.presale.mapper.OrdDisPresaleActivityStoreMapper;
import com.edc.erp.presale.service.OrdDisPresaleActivityStoreService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName OrdDisPresaleActivityStoreServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 15:31
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleActivityStoreServiceImpl extends BaseServiceImpl<OrdDisPresaleActivityStore> implements OrdDisPresaleActivityStoreService {
    private final OrdDisPresaleActivityStoreMapper ordDisPresaleActivityStoreMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSavePresaleActivityStore(List<OrdDisPresaleActivityStore> ordDisPresaleActivityStoreList) {
        ordDisPresaleActivityStoreMapper.batchSavePresaleActivityStore(ordDisPresaleActivityStoreList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByActivityId(Long activityId) {
        ordDisPresaleActivityStoreMapper.deleteByActivityId(activityId);
    }

    @Override
    public List<OrdDisPresaleActivityStore> findStoreListByPresaleActivityId(Long activityId) {
        OrdDisPresaleActivityStore ordDisPresaleActivityStore = new OrdDisPresaleActivityStore();
        ordDisPresaleActivityStore.setPresaleActivityId(activityId);
        ordDisPresaleActivityStore.setIsDelete(ModelConst.DELETE.NO);
        return ordDisPresaleActivityStoreMapper.select(ordDisPresaleActivityStore);
    }
}
