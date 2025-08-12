package com.edc.erp.common.service;


import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.in.async.SaveAsyncTaskIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 异步任务执行(AsyncTask)}表服务接口
 *
 * @author zhangdong
 * @since 2022-08-30 17:20:49
 */
public interface AsyncTaskService extends BaseService<AsyncTask> {

    /**
     * 获取异步任务执行列表（不分页）
     *
     * @param retryMax 搜索条件
     * @return page
     */
    List<AsyncTask> list(Integer retryMax);

    /**
     * 保存异步任务执行信息
     *
     * @param AsyncTask 入参对象
     * @return AsyncTaskVO 数据对象
     */
    AsyncTask save(SaveAsyncTaskIn AsyncTask);

    /**
     * 删除定时任务
     *
     * @param type
     * @param bizId
     * @return
     */
    boolean deleteTask(String type, Integer bizId);


    Future<Response> syncOrderToFund(RechargeLiquidationIn rechargeLiquidationIn);

    List<Long> findTaskIdList(Integer retryMax);

    /**
     * @Description: 按id和条件查询异常待执行的任务
     * @Author: ZhangYao
     * @Date: 2023/4/19 10:43
     * @param id:
     * @param retryMax:
     * @return: com.edc.erp.common.entity.AsyncTask
     **/
    AsyncTask getAsyncTaskByParameter(Long id, Integer retryMax);

    List<Long> findTaskIdListByBizOrgCode(Integer retryMax, String type, String bizOrgCode, Integer limitNum);

    List<AsyncTaskQueryOut> findAsyncTaskListByParameter(AsyncTaskQueryIn asyncTaskQueryIn);

    String updateNeedUpdateZkOrderNoList(String bizOrgCode);

    @Transactional(rollbackFor = Exception.class)
    void rePushZkWholesaleShipmentBackTask(List<Integer> idList, String type);

    Future<Response> syncFrozen(StoreFrozenIn storeFrozenIn);

    Future<Response> syncUnFrozen(UnFrozenIn unFrozenIn);

    List<AsyncTask> findNeedRetryTaskList(List<String> businessNoList, String type);
}
