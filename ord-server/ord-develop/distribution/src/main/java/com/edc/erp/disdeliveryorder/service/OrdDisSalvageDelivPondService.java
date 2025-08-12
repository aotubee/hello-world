package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPond;
import com.edc.erp.disdeliveryorder.model.in.ExecuteDisSalvageDeliveryOrderIn;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdDisSalvageDelivPondService extends BaseService<OrdDisSalvageDelivPond> {


    OrdDisSalvageDelivPond getOneByParameter(Integer orderTypeConfigId, String bizOrgCode, LocalDateTime truncationDateTime);

    @Transactional(rollbackFor = Exception.class)
    OrdDisSalvageDelivPond saveDisSalvageDelivPond(Integer orderTypeConfigId, LocalDateTime truncationDateTime,
                                                   String bizOrgCode, String orgCode, String loginUsername);


    /**
     * 查找需要自动捞单的记录
     *
     * @param executeDisSalvageDeliveryOrderIn
     * @return
     */
    List<OrdDisSalvageDelivPond> findListForAutoSalvage(ExecuteDisSalvageDeliveryOrderIn executeDisSalvageDeliveryOrderIn);

    void updateStatus(OrdDisSalvageDelivPond ordDisSalvageDelivPond);

    List<OrdDisSalvageDelivPond> findNeedRepeatExecuteSalvageList(String salvageStatus, String beginTime, String bizOrgCode);
}
