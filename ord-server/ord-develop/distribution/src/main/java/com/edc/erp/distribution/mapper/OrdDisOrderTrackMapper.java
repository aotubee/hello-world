package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderTrack;
import com.edc.erp.distribution.model.in.DisOrderTrackIn;
import com.edc.erp.distribution.model.out.DisOrderTrackOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销订单追踪表(OrdDisOrderTrack)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
@Repository
public interface OrdDisOrderTrackMapper extends BaseMapper<OrdDisOrderTrack> {

    /**
     * 根据单号查询业务日志
     *
     * @param disOrderTrackIn
     * @return
     */
    List<DisOrderTrackOut> findOrderTrackOutListByParameters(DisOrderTrackIn disOrderTrackIn);
}
