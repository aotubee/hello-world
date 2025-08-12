package com.edc.erp.disdeliveryorder.service.impl;

import com.edc.erp.common.enumeration.SalvageDelivPondStatusEnum;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPond;
import com.edc.erp.disdeliveryorder.mapper.OrdDisSalvageDelivPondMapper;
import com.edc.erp.disdeliveryorder.model.in.ExecuteDisSalvageDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisSalvageDelivPondServiceImpl extends BaseServiceImpl<OrdDisSalvageDelivPond> implements OrdDisSalvageDelivPondService {

    private final OrdDisSalvageDelivPondMapper ordDisSalvageDelivPondMapper;

    private final RedisService redisService;

    @Override
    public OrdDisSalvageDelivPond getOneByParameter(Integer orderTypeConfigId, String bizOrgCode, LocalDateTime truncationDateTime) {
        return ordDisSalvageDelivPondMapper.getOrdDisSalvageDelivPond(orderTypeConfigId, bizOrgCode, truncationDateTime);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDisSalvageDelivPond saveDisSalvageDelivPond(Integer orderTypeConfigId, LocalDateTime truncationDateTime,
                                                          String bizOrgCode, String orgCode, String loginUsername) {
        OrdDisSalvageDelivPond ordDisSalvageDelivPond = this.getOneByParameter(orderTypeConfigId, bizOrgCode, truncationDateTime);
        if (Objects.nonNull(ordDisSalvageDelivPond)) {
            return ordDisSalvageDelivPond;
        }
        ordDisSalvageDelivPond = new OrdDisSalvageDelivPond();
        ordDisSalvageDelivPond.setOrderTypeConfigId(orderTypeConfigId);
        ordDisSalvageDelivPond.setTruncationDateTime(truncationDateTime);
        int delayMinutes = 60;
        String disSalvageDelayMinutesKey = DisSystemConstant.DIS_AUTO_SALVAGE_DELAY_OVERTIME_MINUTES_KEY + bizOrgCode;
        if (StringUtils.isNotBlank(redisService.get(disSalvageDelayMinutesKey))) {
            delayMinutes = Integer.parseInt(redisService.get(disSalvageDelayMinutesKey));
        }
        ordDisSalvageDelivPond.setSalvageTime(truncationDateTime.plusMinutes(delayMinutes));
        ordDisSalvageDelivPond.setSalvageStatus(SalvageDelivPondStatusEnum.WAIT_EXECUTION.getKey());
        ordDisSalvageDelivPond.setBizOrgCode(bizOrgCode);
        ordDisSalvageDelivPond.setOrgCode(orgCode);
        ordDisSalvageDelivPond.setCreator(loginUsername);
        ordDisSalvageDelivPond.setCreateTime(LocalDateTime.now());
        ordDisSalvageDelivPond.setUpdateTime(LocalDateTime.now());
        ordDisSalvageDelivPond.setUpdater(loginUsername);
        ordDisSalvageDelivPond.setIsDelete(ModelConst.DELETE.NO);
//        ordDisSalvageDelivPondMapper.saveOrdDisSalvageDelivPond(ordDisSalvageDelivPond);
        ordDisSalvageDelivPondMapper.insert(ordDisSalvageDelivPond);
        return ordDisSalvageDelivPond;
    }

    @Override
    public List<OrdDisSalvageDelivPond> findListForAutoSalvage(ExecuteDisSalvageDeliveryOrderIn executeDisSalvageDeliveryOrderIn) {
        return ordDisSalvageDelivPondMapper.findListForAutoSalvage(executeDisSalvageDeliveryOrderIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(OrdDisSalvageDelivPond ordDisSalvageDelivPond) {
        ordDisSalvageDelivPondMapper.updateByPrimaryKeySelective(ordDisSalvageDelivPond);
    }

    @Override
    public List<OrdDisSalvageDelivPond> findNeedRepeatExecuteSalvageList(String salvageStatus, String beginTime, String bizOrgCode) {
        return ordDisSalvageDelivPondMapper.findNeedRepeatExecuteSalvageList(salvageStatus, beginTime, bizOrgCode);
    }

}
