package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPond;
import com.edc.erp.directly.dirdeliveryorder.model.in.ExecuteDirSalvageDeliveryOrderIn;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdDirSalvageDelivPondMapper extends BaseMapper<OrdDirSalvageDelivPond> {

    OrdDirSalvageDelivPond getOrdDirSalvageDelivPond(@Param("orderTypeConfigId") Integer orderTypeConfigId,
                                                     @Param("bizOrgCode") String bizOrgCode,
                                                     @Param("truncationDateTime") LocalDateTime truncationDateTime);

    List<OrdDirSalvageDelivPond> findListForAutoSalvage(ExecuteDirSalvageDeliveryOrderIn executeDirSalvageDeliveryOrderIn);

//    int saveOrdDirSalvageDelivPond(OrdDirSalvageDelivPond ordDirSalvageDelivPond);

    List<OrdDirSalvageDelivPond> findNeedRepeatExecuteSalvageList(@Param("salvageStatus") String salvageStatus,
                                                                  @Param("beginTime") String beginTime,
                                                                  @Param("bizOrgCode") String bizOrgCode);
}
