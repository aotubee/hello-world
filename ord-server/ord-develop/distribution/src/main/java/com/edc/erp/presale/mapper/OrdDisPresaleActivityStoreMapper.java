package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleActivityStoreMapper extends BaseMapper<OrdDisPresaleActivityStore> {
    void batchSavePresaleActivityStore(@Param("ordDisPresaleActivityStoreList") List<OrdDisPresaleActivityStore> ordDisPresaleActivityStoreList);

    void deleteByActivityId(@Param("activityId") Long activityId);
}
