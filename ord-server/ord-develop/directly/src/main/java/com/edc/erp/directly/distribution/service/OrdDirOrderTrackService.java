package com.edc.erp.directly.distribution.service;


import com.edc.erp.directly.distribution.entity.OrdDirOrderTrack;
import com.edc.erp.directly.distribution.model.in.DirOrderTrackIn;
import com.edc.erp.directly.distribution.model.out.DirOrderTrackElementOut;
import com.edc.plugins.mybatis.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 直营配货订单追踪表(OrdDisOrderTrack)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
public interface OrdDirOrderTrackService extends BaseService<OrdDirOrderTrack> {

    /**
     * 根据单号查询业务日志
     *
     * @param disOrderTrackIn
     * @return
     */
    List<DirOrderTrackElementOut> findOrderTrackOutListByParameters(DirOrderTrackIn disOrderTrackIn);

    /**
     * 保存订单追踪记录
     *
     * @param orderTrack
     */
    void saveOrderTrack(OrdDirOrderTrack orderTrack);


    /**
     * 缓存订单追踪消息
     *
     * @param orderNo
     * @param storeCode
     * @param orderStatus
     * @param trackLog
     * @param bizOrgCode
     * @param creator
     * @param createTime
     */
    void pushRedisOrderTrackMessage(String orderNo, String storeCode, String orderStatus, String trackLog,
                                    String bizOrgCode, String creator, LocalDateTime createTime);
}
