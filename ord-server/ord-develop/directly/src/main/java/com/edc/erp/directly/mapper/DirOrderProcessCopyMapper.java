package com.edc.erp.directly.mapper;

import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;



/**
 * 直营订单类型流程副本(DirOrderProcessCopy)表数据库访问层
 *
 * @author fxw
 * @since 2023-01-10 16:48:44
 */
@Repository
public interface DirOrderProcessCopyMapper extends BaseMapper<DirOrderProcessCopy> {
//    /**
//     * 保存流程副本
//     *
//     * @param dirOrderProcessCopy
//     */
//    void insertOrderProcessCopy(DirOrderProcessCopy dirOrderProcessCopy);

    /**
     * 根据条件查询一个流程副本
     *
     * @param orderingCycleId
     * @param storeCode
     * @param progressCode
     * @param bizOrgCode
     * @return
     */
    DirOrderProcessCopy getOneByParameter(@Param("orderingCycleId") Integer orderingCycleId,
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
    DirOrderProcessCopy getNextOneByParameter(@Param("orderingCycleId") Integer orderingCycleId,
                                           @Param("storeCode") String storeCode,
                                           @Param("id") Integer id,
                                           @Param("bizOrgCode") String bizOrgCode);
}
