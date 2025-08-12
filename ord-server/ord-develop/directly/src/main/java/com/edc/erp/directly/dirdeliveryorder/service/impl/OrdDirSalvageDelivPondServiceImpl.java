package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.edc.erp.common.enumeration.SalvageDelivPondStatusEnum;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPond;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirSalvageDelivPondMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.ExecuteDirSalvageDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondService;
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
public class OrdDirSalvageDelivPondServiceImpl extends BaseServiceImpl<OrdDirSalvageDelivPond> implements OrdDirSalvageDelivPondService {

    private final OrdDirSalvageDelivPondMapper ordDirSalvageDelivPondMapper;

    private final RedisService redisService;

    @Override
    public OrdDirSalvageDelivPond getOneByParameter(Integer orderTypeConfigId, String bizOrgCode, LocalDateTime truncationDateTime) {
        return ordDirSalvageDelivPondMapper.getOrdDirSalvageDelivPond(orderTypeConfigId, bizOrgCode, truncationDateTime);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirSalvageDelivPond saveDirSalvageDelivPond(Integer orderTypeConfigId, LocalDateTime truncationDateTime,
                                                          String bizOrgCode, String orgCode, String loginUsername) {
        OrdDirSalvageDelivPond ordDirSalvageDelivPond = this.getOneByParameter(orderTypeConfigId, bizOrgCode, truncationDateTime);
        if (Objects.nonNull(ordDirSalvageDelivPond)) {
            return ordDirSalvageDelivPond;
        }
        ordDirSalvageDelivPond = new OrdDirSalvageDelivPond();
        ordDirSalvageDelivPond.setOrderTypeConfigId(orderTypeConfigId);
        ordDirSalvageDelivPond.setTruncationDateTime(truncationDateTime);
        int delayMinutes = 60;
        String dirSalvageDelayMinutesKey = DirSystemConstant.DIR_AUTO_SALVAGE_DELAY_OVERTIME_MINUTES_KEY + bizOrgCode;
        if (StringUtils.isNotBlank(redisService.get(dirSalvageDelayMinutesKey))) {
            delayMinutes = Integer.parseInt(redisService.get(dirSalvageDelayMinutesKey));
        }
        ordDirSalvageDelivPond.setSalvageTime(truncationDateTime.plusMinutes(delayMinutes));
        ordDirSalvageDelivPond.setSalvageStatus(SalvageDelivPondStatusEnum.WAIT_EXECUTION.getKey());
        ordDirSalvageDelivPond.setBizOrgCode(bizOrgCode);
        ordDirSalvageDelivPond.setOrgCode(orgCode);
        ordDirSalvageDelivPond.setCreator(loginUsername);
        ordDirSalvageDelivPond.setCreateTime(LocalDateTime.now());
        ordDirSalvageDelivPond.setUpdateTime(LocalDateTime.now());
        ordDirSalvageDelivPond.setUpdater(loginUsername);
        ordDirSalvageDelivPond.setIsDelete(ModelConst.DELETE.NO);
//        ordDirSalvageDelivPondMapper.saveOrdDirSalvageDelivPond(ordDirSalvageDelivPond);
        ordDirSalvageDelivPondMapper.insert(ordDirSalvageDelivPond);
//        ordDirSalvageDelivPond = this.getOneByParameter(orderTypeConfigId,bizOrgCode, truncationDateTime);
        return ordDirSalvageDelivPond;
    }

    @Override
    public List<OrdDirSalvageDelivPond> findListForAutoSalvage(ExecuteDirSalvageDeliveryOrderIn executeDirSalvageDeliveryOrderIn) {
        return ordDirSalvageDelivPondMapper.findListForAutoSalvage(executeDirSalvageDeliveryOrderIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(OrdDirSalvageDelivPond ordDirSalvageDelivPond) {
        ordDirSalvageDelivPondMapper.updateByPrimaryKeySelective(ordDirSalvageDelivPond);
    }

    @Override
    public List<OrdDirSalvageDelivPond> findNeedRepeatExecuteSalvageList(String salvageStatus, String beginTime, String bizOrgCode) {
        return ordDirSalvageDelivPondMapper.findNeedRepeatExecuteSalvageList(salvageStatus, beginTime, bizOrgCode);
    }

}
