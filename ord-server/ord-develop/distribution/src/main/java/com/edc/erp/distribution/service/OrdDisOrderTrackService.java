package com.edc.erp.distribution.service;

import com.edc.erp.distribution.entity.OrdDisOrderTrack;
import com.edc.erp.distribution.model.in.DisOrderTrackIn;
import com.edc.erp.distribution.model.out.DisOrderTrackElementOut;
import com.edc.plugins.mybatis.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;



/**
 * 配销订单追踪表(OrdDisOrderTrack)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
public interface OrdDisOrderTrackService extends BaseService<OrdDisOrderTrack> {

    /**
     * 根据单号查询业务日志
     *
     * @param disOrderTrackIn
     * @return
     */
    List<DisOrderTrackElementOut> findOrderTrackOutListByParameters(DisOrderTrackIn disOrderTrackIn);

    /**
     * 保存订单追踪记录
     *
     * @param orderTrack
     */
    void saveOrderTrack(OrdDisOrderTrack orderTrack);


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
