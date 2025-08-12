package com.edc.erp.directly.service;

import com.edc.erp.directly.entity.DirOrderProcess;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.model.out.DirOrderProcessOut;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 订单类型流程(OrderProcess)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
public interface DirOrderProcessService extends BaseService<DirOrderProcess> {

    /**
     * 根据订单流程配置id和业务组织编码查询订单流程
     *
     * @param dirOrderTypeConfig
     * @param bizOrgCode
     * @return
     */
    List<DirOrderProcessOut> findProcessListByOrderTypeConfigId(DirOrderTypeConfig dirOrderTypeConfig, String bizOrgCode);

    /**
     * 根据订单id查询订单类型流程所有信息
     * @param orderTypeConfigId  订单类型主键
     * @param bizOrgCode    业务组织
     * @return
     */
    List<DirOrderProcessOut> findProcessOutListByTypeConfigIdAndOrg(Long orderTypeConfigId, String bizOrgCode);
}
