package com.edc.erp.distribution.service.impl;

import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.mapper.OrdDisOrderCycleMapper;
import com.edc.erp.distribution.model.in.AppQueryDisOrderIn;
import com.edc.erp.distribution.model.out.DisOrderCycleOrderOut;
import com.edc.erp.distribution.service.OrdDisOrderCycleService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * 配销订货周期(OrdDisOrderCycle)表服务实现类
 *
 * @author fxw
 * @since 2022-10-17 16:49:33
 */
@Service
public class OrdDisOrderCycleServiceImpl extends BaseServiceImpl<OrdDisOrderCycle> implements OrdDisOrderCycleService {

    @Autowired
    private OrdDisOrderCycleMapper ordDisOrderCycleMapper;

    /**
     * 根据id和业务组织编码获取配销订货周期信息
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDisOrderCycle getOrderCycleByIdAndBizOrgCode(Integer id, String bizOrgCode) {
        OrdDisOrderCycle ordDisOrderCycle = new OrdDisOrderCycle();
        ordDisOrderCycle.setId(id);
        ordDisOrderCycle.setBizOrgCode(bizOrgCode);
        ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderCycleMapper.selectOne(ordDisOrderCycle);
    }

    /**
     * 根据条件查询订货周期
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param truncationTime
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDisOrderCycle getOrderCycle(String storeCode, Integer orderTypeConfigId, LocalDateTime truncationTime, String bizOrgCode) {
        OrdDisOrderCycle ordDisOrderCycle = new OrdDisOrderCycle();
        ordDisOrderCycle.setStoreCode(storeCode);
        ordDisOrderCycle.setOrderTypeConfigId(orderTypeConfigId);
        ordDisOrderCycle.setTruncationDateTime(truncationTime);
        ordDisOrderCycle.setBizOrgCode(bizOrgCode);
        ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderCycleMapper.selectOne(ordDisOrderCycle);
    }

    /**
     * 创建订货周期
     *
     * @param orderCycle
     */
    @Override
    public OrdDisOrderCycle createOrderCycle(OrdDisOrderCycle orderCycle) {
        if (Objects.isNull(orderCycle.getId())) {
            ordDisOrderCycleMapper.insert(orderCycle);
        }
        return orderCycle;
    }

    @Override
    public List<OrdDisOrderCycle> findOrderCycleListBetweenCreateTime(String beginTime, String endTime, String bizOrgCode, List<String> orderStatusCodeList) {
        return ordDisOrderCycleMapper.findOrderCycleListBetweenCreateTime(beginTime, endTime, bizOrgCode, orderStatusCodeList);
    }

    @Override
    public List<DisOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDisOrderIn appQueryOrderIn) {
        return ordDisOrderCycleMapper.findOrderCycleOrderListByAppQueryOrderIn(appQueryOrderIn);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFirstOrderTime(Integer id, LocalDateTime firstOrderTime, String loginUsername) {
        OrdDisOrderCycle ordDisOrderCycle = new OrdDisOrderCycle();
        ordDisOrderCycle.setId(id);
        ordDisOrderCycle.setFirstOrderTime(firstOrderTime);
        ordDisOrderCycle.setUpdater(loginUsername);
        ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
        ordDisOrderCycleMapper.updateByPrimaryKeySelective(ordDisOrderCycle);
    }

    @Override
    public List<OrdDisOrderCycle> findByOrderIds(String ids) {
        return ordDisOrderCycleMapper.findByOrderIds(ids);
    }
}
