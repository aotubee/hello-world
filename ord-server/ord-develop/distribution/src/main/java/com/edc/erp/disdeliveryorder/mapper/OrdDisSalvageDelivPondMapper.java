package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPond;
import com.edc.erp.disdeliveryorder.model.in.ExecuteDisSalvageDeliveryOrderIn;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdDisSalvageDelivPondMapper extends BaseMapper<OrdDisSalvageDelivPond> {

    OrdDisSalvageDelivPond getOrdDisSalvageDelivPond(@Param("orderTypeConfigId") Integer orderTypeConfigId,
                                                     @Param("bizOrgCode") String bizOrgCode,
                                                     @Param("truncationDateTime") LocalDateTime truncationDateTime);

    List<OrdDisSalvageDelivPond> findListForAutoSalvage(ExecuteDisSalvageDeliveryOrderIn executeDisSalvageDeliveryOrderIn);

//    int saveOrdDisSalvageDelivPond(OrdDisSalvageDelivPond ordDisSalvageDelivPond);

    List<OrdDisSalvageDelivPond> findNeedRepeatExecuteSalvageList(@Param("salvageStatus") String salvageStatus,
                                                                  @Param("beginTime") String beginTime,
                                                                  @Param("bizOrgCode") String bizOrgCode);
}
