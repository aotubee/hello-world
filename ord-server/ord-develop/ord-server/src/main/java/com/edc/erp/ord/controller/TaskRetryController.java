package com.edc.erp.ord.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.service.AsyncTaskService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.ord.handle.DirTaskRetryHandle;
import com.edc.erp.ord.handle.DisTaskRetryHandle;
import com.edc.erp.ord.model.in.MqTaskRetryIn;
import com.edc.plugins.common.response.Response;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.StringJoiner;

/**
 * @ClassName TaskController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/7/16 9:09
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/ord/taskRetry")
@Slf4j
public class TaskRetryController {

    private final AsyncTaskService asyncTaskService;
    private final DirTaskRetryHandle dirTaskRetryHandle;
    private final DisTaskRetryHandle disTaskRetryHandle;

    @ApiOperation(value = "task任务补偿", notes = "task任务补偿")
    @PostMapping("/mqTaskRetry")
    public Response<String> mqTaskRetry(@RequestBody @Valid MqTaskRetryIn mqTaskRetryIn) {
        List<String> businessNoList = Lists.newArrayList(mqTaskRetryIn.getTaskBusinessNos().split(SystemConstant.COMMA));
        List<AsyncTask> needRetryTaskList = asyncTaskService.findNeedRetryTaskList(businessNoList, mqTaskRetryIn.getTaskType());
        if (CollectionUtils.isEmpty(needRetryTaskList)) {
            return Response.data("没有可处理的任务");
        }
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        needRetryTaskList.forEach(asyncTask -> {
            try {
                //  直营
                if (NumberUtil.INTEGER_ONE.equals(mqTaskRetryIn.getStoreType())) {
                    dirTaskRetryHandle.retryTask(asyncTask);
                }
                //  加盟
                if (NumberUtil.INTEGER_TWO.equals(mqTaskRetryIn.getStoreType())) {
                    disTaskRetryHandle.retryTask(asyncTask);
                }
            } catch (Exception e) {
                errorJoiner.add(asyncTask.getBusinessNo());
                log.error("任务ID{}执行异常", asyncTask.getId(), e);
            }
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.error(errorJoiner.toString());
        } else {
            return Response.success();
        }
    }


}
