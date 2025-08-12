package com.edc.erp.directly.distribution.service.impl;

import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderCycleMapper;
import com.edc.erp.directly.distribution.model.in.AppQueryDirOrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderCycleOrderOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderCycleService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 订货周期(OrdDirOrderCycle)表服务实现类
 *
 * @author wanglidong
 * @since 2022-11-16 14:27:55
 */
@Service
public class OrdDirOrderCycleServiceImpl extends BaseServiceImpl<OrdDirOrderCycle> implements OrdDirOrderCycleService {

    @Autowired
    private OrdDirOrderCycleMapper ordDirOrderCycleMapper;

    /**
     * 根据id和业务组织编码获取直营订货周期信息
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDirOrderCycle getOrderCycleByIdAndBizOrgCode(Integer orderCycleId, String bizOrgCode) {
        OrdDirOrderCycle ordDisOrderCycle = new OrdDirOrderCycle();
        ordDisOrderCycle.setId(orderCycleId);
        ordDisOrderCycle.setBizOrgCode(bizOrgCode);
        ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderCycleMapper.selectOne(ordDisOrderCycle);
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
    public OrdDirOrderCycle getOrderCycle(String storeCode, Integer orderTypeConfigId, LocalDateTime truncationTime, String bizOrgCode) {
        OrdDirOrderCycle ordDirOrderCycle = new OrdDirOrderCycle();
        ordDirOrderCycle.setStoreCode(storeCode);
        ordDirOrderCycle.setOrderTypeConfigId(orderTypeConfigId);
        ordDirOrderCycle.setTruncationDateTime(truncationTime);
        ordDirOrderCycle.setBizOrgCode(bizOrgCode);
        ordDirOrderCycle.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderCycleMapper.selectOne(ordDirOrderCycle);
    }

    /**
     * 创建订货周期
     *
     * @param orderCycle
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirOrderCycle createOrderCycle(OrdDirOrderCycle orderCycle) {
        if (Objects.isNull(orderCycle.getId())) {
            ordDirOrderCycleMapper.insert(orderCycle);
        }
//        return this.getOrderCycle(orderCycle.getStoreCode(), orderCycle.getOrderTypeConfigId(),
//                orderCycle.getTruncationDateTime(), orderCycle.getBizOrgCode());
        return orderCycle;
    }

    @Override
    public List<OrdDirOrderCycle> findOrderCycleListBetweenCreateTime(String beginTime, String endTime, String bizOrgCode, List<String> orderStatusCodeList) {
        return ordDirOrderCycleMapper.findOrderCycleListBetweenCreateTime(beginTime, endTime, bizOrgCode, orderStatusCodeList);
    }

    @Override
    public List<DirOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDirOrderIn appQueryOrderIn) {
        return ordDirOrderCycleMapper.findOrderCycleOrderListByAppQueryOrderIn(appQueryOrderIn);
    }

    @Override
    public List<OrdDirOrderCycle> findByOrderIds(String ids) {
        return ordDirOrderCycleMapper.findByOrderIds(ids);
    }
}
