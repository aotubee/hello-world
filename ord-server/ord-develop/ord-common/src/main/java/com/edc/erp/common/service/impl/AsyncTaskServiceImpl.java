package com.edc.erp.common.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.enumeration.AsyncTaskEnum;
import com.edc.erp.common.mapper.AsyncTaskMapper;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.in.async.SaveAsyncTaskIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.service.AsyncTaskService;
import com.edc.erp.common.service.FundServer;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Future;

/**
 * 异步任务执行(AsyncTask)表服务实现类
 *
 * @author zhangdong
 * @since 2022-08-30 17:20:51
 */
@Slf4j
@Service
public class AsyncTaskServiceImpl extends BaseServiceImpl<AsyncTask> implements AsyncTaskService {

    @Autowired
    private AsyncTaskMapper asyncTaskMapper;

    @Autowired
    private FundServer fundServer;

    /**
     * 获取异步任务执行列表（不分页）
     *
     * @param retryMax 搜索条件
     * @return page
     */
    @Override
    public List<AsyncTask> list(Integer retryMax) {
        return asyncTaskMapper.findList(retryMax);
    }

    /**
     * 保存异步任务执行信息
     *
     * @param saveAsyncTaskIn 入参对象
     * @return AsyncTaskVO 数据对象
     */
    @Override
    public AsyncTask save(SaveAsyncTaskIn saveAsyncTaskIn) {
        AsyncTask xxJob = saveAsyncTaskIn.getAsyncTask();
        super.insertSelective(xxJob);
        AsyncTask data = new AsyncTask();
        BeanUtils.copy(xxJob, data);
        return data;
    }

    @Override
    public boolean deleteTask(String type, Integer bizId) {
        return asyncTaskMapper.deleteTask(type, bizId) >= 0;
    }

    /**
     * 调资管中心清算
     */
    @Async
    @Override
    public Future<Response> syncOrderToFund(RechargeLiquidationIn rechargeLiquidationIn) {
        Response response = fundServer.settlement(rechargeLiquidationIn);
        log.info("调用资管清算返回结果" + response + "---------------------------");
        return new AsyncResult<>(response);

    }

    @Override
    public List<Long> findTaskIdList(Integer retryMax) {
        return asyncTaskMapper.findTaskIdList(retryMax);
    }

    @Override
    public AsyncTask getAsyncTaskByParameter(Long id, Integer retryMax) {
        return asyncTaskMapper.getAsyncTaskByParameter(id, retryMax);
    }

    @Override
    public List<Long> findTaskIdListByBizOrgCode(Integer retryMax, String type, String bizOrgCode, Integer limitNum) {
        return asyncTaskMapper.findTaskIdListByBizOrgCode(retryMax, type, bizOrgCode, limitNum);
    }

    @Override
    public List<AsyncTaskQueryOut> findAsyncTaskListByParameter(AsyncTaskQueryIn asyncTaskQueryIn) {
        return asyncTaskMapper.findAsyncTaskListByParameter(asyncTaskQueryIn);
    }

    @Override
    public String updateNeedUpdateZkOrderNoList(String bizOrgCode) {
        StringJoiner stringJoiner = new StringJoiner(",");
        List<AsyncTask> list = asyncTaskMapper.findNeedUpdateZkOrderNoList(bizOrgCode);
        list.forEach(asyncTask -> {
            JSONObject jsonObject = JSONObject.parseObject(asyncTask.getMessageJson());
            JSONObject zkJsonObject = null;
            if (AsyncTaskEnum.ZK_SAVE_WHOLESALE_SHIPMENT.getCode().equals(asyncTask.getType())) {
                zkJsonObject = jsonObject.getJSONObject("zkWholesaleShipmentIn");
            }
            if (AsyncTaskEnum.ZK_SAVE_WHOLESALE_RETURN.getCode().equals(asyncTask.getType())) {
                zkJsonObject = jsonObject.getJSONObject("zkWholesaleReturnIn");
            }
            if (zkJsonObject.containsKey("sourceNo")) {
                String sourceNo = zkJsonObject.getString("sourceNo");
                if (StringUtils.isNotBlank(sourceNo)) {
                    try {
                        asyncTaskMapper.updateRemarkById(sourceNo, asyncTask.getId());
                    } catch (Exception e) {
                        log.error("任务{}更新中科单号异常", asyncTask.getId());
                        stringJoiner.add(asyncTask.getId().toString());
                    }
                }
            }
        });
        return stringJoiner.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rePushZkWholesaleShipmentBackTask(List<Integer> idList, String type) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        asyncTaskMapper.batchUpdate(idList, type);
    }

    @Async
    @Override
    public Future<Response> syncFrozen(StoreFrozenIn storeFrozenIn) {
        Response response = fundServer.frozen(storeFrozenIn);
        log.info("调用资管冻结返回结果" + response + "---------------------------");
        return new AsyncResult<>(response);

    }

    @Async
    @Override
    public Future<Response> syncUnFrozen(UnFrozenIn unFrozenIn) {
        Response response = fundServer.unFrozen(unFrozenIn);
        log.info("调用资管解冻返回结果" + response + "---------------------------");
        return new AsyncResult<>(response);

    }

    @Override
    public List<AsyncTask> findNeedRetryTaskList(List<String> businessNoList, String type) {
        return asyncTaskMapper.findNeedRetryTaskList(businessNoList, type);
    }
}
