package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderTrack;
import com.edc.erp.directly.distribution.model.in.DirOrderTrackIn;
import com.edc.erp.directly.distribution.model.out.DirOrderTrackOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 直营订单追踪表(OrdDirOrderTrack)表数据库访问层
 *
 * @author fxw
 * @since 2022-11-18 18:46:57
 */
@Repository
public interface OrdDirOrderTrackMapper extends BaseMapper<OrdDirOrderTrack> {

    /**
     * 根据单号查询业务日志
     *
     * @param disOrderTrackIn
     * @return
     */
    List<DirOrderTrackOut> findOrderTrackOutListByParameters(DirOrderTrackIn disOrderTrackIn);
}
