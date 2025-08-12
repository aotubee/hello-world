package com.edc.erp.disfirstorder.mapper;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstIn;
import com.edc.erp.disfirstorder.model.out.FirstOrderConfigOut;
import com.edc.erp.disfirstorder.model.out.FirstOrderSortOut;
import com.edc.erp.disfirstorder.model.out.OrdDerDisOrderFirstOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销铺货单(OrdDisOrderFirst)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
@Repository
public interface OrdDisOrderFirstMapper extends BaseMapper<OrdDisOrderFirst> {
    /**
     * 获取首单铺货订单列表
     * @param ordDisOrderFirstIn
     * @return
     */
    List<OrdDerDisOrderFirstOut> findFirstOrderByPage(OrdDisOrderFirstIn ordDisOrderFirstIn);

    /**
     * 测试查询铺货配置
     * @param bizOrgCode
     * @return
     */
    FirstOrderConfigOut getOrderFirstConfig(String bizOrgCode);

    /**
     * 试查询铺货配置品类分类
     * @param id
     * @return
     */
    List<FirstOrderSortOut> findOrderFirstConfigSort(Integer id);

    /**
     * 根据条件查询已审核铺货单
     * @param ordDisOrderFirst
     * @return
     */
    List<OrdDisOrderFirst> selectByEffectiveTime(OrdDisOrderFirst ordDisOrderFirst);

    OrdDisOrderFirst getOrdDisFirstByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);

    int unFreezeDisFirstOrder(@Param("order") OrdDisOrderFirst ordDisOrderFirst);


}
