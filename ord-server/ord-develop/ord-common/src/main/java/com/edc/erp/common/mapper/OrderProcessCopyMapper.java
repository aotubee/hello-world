package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * 订单类型流程副本(OrderProcessCopy)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 16:35:14
 */
@Repository
public interface OrderProcessCopyMapper extends BaseMapper<OrderProcessCopy> {

//    /**
//     * 保存流程副本
//     *
//     * @param orderProcessCopy
//     */
//    void insertOrderProcessCopy(OrderProcessCopy orderProcessCopy);

    /**
     * 根据条件查询一个流程副本
     *
     * @param orderingCycleId
     * @param storeCode
     * @param progressCode
     * @param bizOrgCode
     * @return
     */
    OrderProcessCopy getOneByParameter(@Param("orderingCycleId") Integer orderingCycleId,
                                       @Param("storeCode") String storeCode,
                                       @Param("progressCode") String progressCode,
                                       @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件查询下一个流程副本
     * @param orderingCycleId
     * @param storeCode
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrderProcessCopy getNextOneByParameter(@Param("orderingCycleId") Integer orderingCycleId,
                                           @Param("storeCode") String storeCode,
                                           @Param("id") Integer id,
                                           @Param("bizOrgCode") String bizOrgCode);
}
