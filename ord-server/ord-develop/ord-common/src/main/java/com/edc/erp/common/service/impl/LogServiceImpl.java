package com.edc.erp.common.service.impl;

import com.edc.erp.common.service.LogService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.rpc.LogServerApi;
import com.edc.sdk.log.vo.BusinessLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: 日志接口服务实现
 * @author fxw
 * @since 2022/09/17
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogServerApi logServerApi;

    @Override
    public BusinessLog saveLog(BusinessLog businessLog) {
        Response<BusinessLog> response = logServerApi.saveLog(businessLog);
        if (null != response && response.isSuccess()) {
            return response.getData();
        }
        return null;
    }

    @Override
    public Page<BusinessLog> boolQueryByPage(BusinessLogVO businessLogIn) {
        Response<Page<BusinessLog>> response = logServerApi.boolQueryByPage(businessLogIn);
        if (null != response && response.isSuccess()) {
            return response.getData();
        }
        return null;
    }
}
