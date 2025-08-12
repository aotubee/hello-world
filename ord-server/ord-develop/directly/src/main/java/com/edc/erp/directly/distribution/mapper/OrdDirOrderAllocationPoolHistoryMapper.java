package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPoolHistory;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName OrdDirOrderAllocationPoolMapper
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 8:56
 **/
@Repository
public interface OrdDirOrderAllocationPoolHistoryMapper extends BaseMapper<OrdDirOrderAllocationPoolHistory> {

    List<OrdDirOrderAllocationPoolHistory> findHistoryListByPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn);


}
