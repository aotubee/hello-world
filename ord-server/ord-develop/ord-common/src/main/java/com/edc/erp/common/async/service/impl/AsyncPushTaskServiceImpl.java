package com.edc.erp.common.async.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.enumeration.AsyncTaskEnum;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.in.async.SaveAsyncTaskIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;
import com.edc.erp.common.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description pur-com.edc.erp.pur.common.asyn.service.impl
 * @Author ZhangDong
 * @Date 2022年08月31日 11:37 星期三
 */
@Service
@Slf4j
public class AsyncPushTaskServiceImpl implements AsyncPushTaskService {
    @Autowired
    private AsyncTaskService asyncTaskService;

    @Value("#{${asyn-task.interval}}")
    private Map<Integer, Integer> interval;

    @Override
    public void submit(String type, String messageJson, String bizOrgCode, String businessNo) {
        AsyncTask asyncTask = new AsyncTask();
        asyncTask.setType(type);
        asyncTask.setMessageJson(messageJson);
        asyncTask.setBusinessNo(businessNo);
        asyncTask.setRetryNo(NumberUtils.INTEGER_ONE);
        asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.UNEXECUTED);
        Integer intervalMinute = interval.get(NumberUtils.INTEGER_ONE);
        DateTime dateTime = DateUtil.offsetMinute(new Date(), intervalMinute);
        asyncTask.setNextRetryTime(dateTime);
        asyncTask.setBizOrgCode(bizOrgCode);
        this.submit(asyncTask);
    }


    @Override
    public void submit(AsyncTask asyncTask) {
        SaveAsyncTaskIn saveAsyncTaskIn = new SaveAsyncTaskIn();
        saveAsyncTaskIn.setAsyncTask(asyncTask);
        asyncTaskService.save(saveAsyncTaskIn);
    }

    @Override
    public List<AsyncTaskQueryOut> findAsyncTaskListByParameter(AsyncTaskQueryIn asyncTaskQueryIn) {
        List<AsyncTaskQueryOut> list = asyncTaskService.findAsyncTaskListByParameter(asyncTaskQueryIn);
        list.forEach(asyncTaskQueryOut -> {
            asyncTaskQueryOut.setTypeStr(AsyncTaskEnum.getNameByCode(asyncTaskQueryOut.getType()));
        });
        return list;
    }

    @Override
    public void submitForMQ(String type, String messageJson, String bizOrgCode, String businessNo, boolean resultFlag, String remark) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(bizOrgCode) || StringUtils.isBlank(businessNo)) {
            log.error("参数异常type{}--bizOrgCode{}--businessNo{}", type, bizOrgCode, businessNo);
            return;
        }
        if (AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE.equals(type) || AsyncTaskConstant.Type.ORDER_DIS_DELIVERY_DATA_FILE.equals(type)) {
            this.submit(type, messageJson, bizOrgCode, businessNo);
            return;
        }
        String status = resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL;
        AsyncTask asyncTask = this.countOne(type, bizOrgCode, businessNo);
        if (Objects.isNull(asyncTask)) {
            asyncTask = new AsyncTask();
            asyncTask.setType(type);
            asyncTask.setMessageJson(messageJson);
            asyncTask.setBusinessNo(businessNo);
            asyncTask.setRetryNo(NumberUtils.INTEGER_ONE);
//          asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.UNEXECUTED);
            asyncTask.setExecStatus(status);
//            Integer intervalMinute = interval.get(NumberUtils.INTEGER_ONE);
//          DateTime dateTime = DateUtil.offsetMinute(new Date(), intervalMinute);
            asyncTask.setNextRetryTime(DateUtil.date());
            asyncTask.setBizOrgCode(bizOrgCode);
            asyncTask.setRemark(remark);
            this.submit(asyncTask);
        } else {
            asyncTask.setExecStatus(status);
            asyncTask.setNextRetryTime(DateUtil.date());
            asyncTaskService.updateByPrimaryKey(asyncTask);
        }

    }

    private AsyncTask countOne(String type, String bizOrgCode, String businessNo) {
        AsyncTask asyncTask = new AsyncTask();
        asyncTask.setType(type);
        asyncTask.setBizOrgCode(bizOrgCode);
        asyncTask.setBusinessNo(businessNo);
        return asyncTaskService.selectOne(asyncTask);
    }
}
