package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrdDirOrderAllocationPoolMapper
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 8:56
 **/
@Repository
public interface OrdDirOrderAllocationPoolMapper extends BaseMapper<OrdDirOrderAllocationPool> {

    List<String> findStoreList();

    List<OrdDirOrderAllocationPool> findEmptyTruncationDateTimeListByStoreCode(@Param("storeCode") String storeCode);

    List<OrdDirOrderAllocationPool> findStoreTruncationDateTimeList();

    List<OrdDirOrderAllocationPool> findListByStoreCodeAndTruncationDateTime(@Param("storeCode") String storeCode, @Param("truncationDateTime") LocalDateTime truncationDateTime);

    List<OrdDirOrderAllocationPoolPageOut> findListByPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn);

    int deleteById(@Param("id") Long id);

    List<String> findDirOrderAllocationPoolSortList(@Param("bizOrgCode") String bizOrgCode);

}
