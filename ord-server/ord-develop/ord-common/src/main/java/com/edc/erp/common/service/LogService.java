package com.edc.erp.common.service;

import com.edc.plugins.common.model.page.Page;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.vo.BusinessLogVO;

/**
 * @description: 日志接口服务
 * @author fxw
 * @since 2022/09/17
 */
public interface LogService {

    /**
     * 保存业务日志
     *
     * @param businessLog
     * @return
     */
    BusinessLog saveLog(BusinessLog businessLog);

    /**
     * 分布查询业务日志
     *
     * @param businessLogIn
     * @return
     */
    Page<BusinessLog> boolQueryByPage(BusinessLogVO businessLogIn);
}
