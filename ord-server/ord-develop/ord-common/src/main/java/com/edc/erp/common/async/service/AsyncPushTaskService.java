package com.edc.erp.common.async.service;


import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;

import java.util.List;

/**
 * @description: 异步提交推送信息
 * @author fxw
 * @since 2022-11-22
 */
public interface AsyncPushTaskService {

    /**
     * 提交任务信息
     *
     * @param type
     * @param messageJson
     * @param bizOrgCode
     * @param businessNo
     */
    void submit(String type,String messageJson, String bizOrgCode, String businessNo);

    /**
     * 任务提交
     *
     * @param AsyncTask
     */
    void submit(AsyncTask AsyncTask);

    List<AsyncTaskQueryOut> findAsyncTaskListByParameter(AsyncTaskQueryIn asyncTaskQueryIn);

    void submitForMQ(String type, String messageJson, String bizOrgCode, String businessNo, boolean resultFlag, String remark);
}
