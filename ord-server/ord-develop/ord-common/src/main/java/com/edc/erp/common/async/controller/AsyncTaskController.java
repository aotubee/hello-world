package com.edc.erp.common.async.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.in.zk.ZkBussinessOrderPushIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;
import com.edc.erp.common.service.AsyncTaskService;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName AsyncTaskController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/3/2 10:41
 **/
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/asyncTask")
@Api(value = "单据任务", tags = "单据任务中间层")
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final RedisService redisService;

    @ApiOperation(value = "查询单据任务中间层", notes = "查询单据任务中间层")
    @GetMapping("/findAsyncTaskListByParameter")
    public Response<List<AsyncTaskQueryOut>> findAsyncTaskListByParameter(@Valid AsyncTaskQueryIn asyncTaskQueryIn) {
        if (Objects.isNull(asyncTaskQueryIn)) {
            return Response.error("请输入查询参数");
        }
        if (Objects.isNull(asyncTaskQueryIn.getIsError()) && StringUtils.isBlank(asyncTaskQueryIn.getBusinessNo())
                && StringUtils.isBlank(asyncTaskQueryIn.getRemark())) {
            return Response.error("请填写ERP单号或者备注");
        }
        asyncTaskQueryIn.setBizOrgCode(UserUtil.getBizOrgCode());
        List<AsyncTaskQueryOut> list = asyncPushTaskService.findAsyncTaskListByParameter(asyncTaskQueryIn);
        return Response.data(list);
    }

    @GetMapping("/updateNeedUpdateZkOrderNoList")
    public Response<String> updateNeedUpdateZkOrderNoList(@RequestParam String bizOrgCode) {
        String errorIdStr = asyncTaskService.updateNeedUpdateZkOrderNoList(bizOrgCode);
        return Response.data(errorIdStr);
    }


    @GetMapping("/getRedisValue")
    public Response<String> getRedisValue(@RequestBody JSONObject jsonObject) {
        String type = jsonObject.getString("type");
        String bizOrgCode = jsonObject.getString("bizOrgCode");
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        jsonArray.forEach(s -> {
            String key = type + SystemConstant.COLON + bizOrgCode + SystemConstant.COLON + s;
            redisService.del(key);
        });
        return Response.success();
    }

    @ApiOperation(value = "重推批发出发货发送中科", notes = "重推批发出发货发送中科")
    @PostMapping("/rePushZkWholesaleShipmentBackTask")
    public Response<String> rePushZkWholesaleShipmentBackTask(@RequestBody @Valid ZkBussinessOrderPushIn zkBussinessOrderPushIn) {
        if (Objects.isNull(zkBussinessOrderPushIn)) {
            return Response.error("参数为空");
        }
        asyncTaskService.rePushZkWholesaleShipmentBackTask(zkBussinessOrderPushIn.getIdList(), zkBussinessOrderPushIn.getType());
        return Response.success();
    }

}
