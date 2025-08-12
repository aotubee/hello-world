package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.model.in.CheckExistPresaleActivityIn;
import com.edc.erp.presale.model.in.PresaleActivityListPageIn;
import com.edc.erp.presale.model.out.CheckExistPresaleActivityOut;
import com.edc.erp.presale.model.out.PresaleActivityListOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleActivityMapper extends BaseMapper<OrdDisPresaleActivity> {

    List<Long> findNeedExecutePresaleActivityIdList();

    int updatePresaleActivityStatus(@Param("activity") OrdDisPresaleActivity ordDisPresaleActivity, @Param("beforeStatus") String beforeStatus);

    List<CheckExistPresaleActivityOut> findExistPresaleActivityInfo(CheckExistPresaleActivityIn checkExistPresaleActivityIn);

    List<Long> findNeedStopPresaleActivityIdList();

    List<OrdDisPresaleActivity> findExecutePresaleActivityImageList(@Param("storeCode") String storeCode);

    List<OrdDisPresaleActivity> findPresaleActivityInfoList(@Param("storeCode") String storeCode);

    List<PresaleActivityListOut> findPresaleActivityListByPage(PresaleActivityListPageIn presaleActivityListIn);
}