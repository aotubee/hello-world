package com.edc.erp.directly.distribution.service;

import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.plugins.common.model.page.Page;

/**
 * @ClassName OrdDirOrderAllocationPoolHistoryService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/8 9:16
 **/
public interface OrdDirOrderAllocationPoolHistoryService {

    Page<OrdDirOrderAllocationPoolPageOut> findHistoryListForPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn);

    String exportAllocationPoolHistory(QueryAllocationPoolPageIn queryAllocationPoolPageIn);
}
