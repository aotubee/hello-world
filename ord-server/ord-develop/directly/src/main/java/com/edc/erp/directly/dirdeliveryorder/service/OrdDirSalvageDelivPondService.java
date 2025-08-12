package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPond;
import com.edc.erp.directly.dirdeliveryorder.model.in.ExecuteDirSalvageDeliveryOrderIn;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdDirSalvageDelivPondService extends BaseService<OrdDirSalvageDelivPond> {


    OrdDirSalvageDelivPond getOneByParameter(Integer orderTypeConfigId, String bizOrgCode, LocalDateTime truncationDateTime);

    @Transactional(rollbackFor = Exception.class)
    OrdDirSalvageDelivPond saveDirSalvageDelivPond(Integer orderTypeConfigId, LocalDateTime truncationDateTime,
                                                   String bizOrgCode, String orgCode, String loginUsername);


    /**
     * 查找需要自动捞单的记录
     *
     * @param executeDirSalvageDeliveryOrderIn
     * @return
     */
    List<OrdDirSalvageDelivPond> findListForAutoSalvage(ExecuteDirSalvageDeliveryOrderIn executeDirSalvageDeliveryOrderIn);

    void updateStatus(OrdDirSalvageDelivPond ordDirSalvageDelivPond);

    List<OrdDirSalvageDelivPond> findNeedRepeatExecuteSalvageList(String salvageStatus, String beginTime, String bizOrgCode);
}
