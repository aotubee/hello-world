package com.edc.erp.common.mapper.dss;


import com.edc.erp.common.model.out.DssOrderInfoOut;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DssOrderInfoMapper {

    List<DssOrderInfoOut> findDssOrderInfoList(@Param("storeCodeList") List<String> storeCodeList,
                                               @Param("beginTime") String beginTime, @Param("endTime") String endTime);

}
