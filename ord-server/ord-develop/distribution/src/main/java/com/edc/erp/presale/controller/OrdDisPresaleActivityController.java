package com.edc.erp.presale.controller;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.enumeration.OrdDisPresaleActivityStatusEnum;
import com.edc.erp.presale.model.in.ExtendOrderDateIn;
import com.edc.erp.presale.model.in.PresaleActivityListPageIn;
import com.edc.erp.presale.model.in.SaveDisPresaleActivityIn;
import com.edc.erp.presale.model.out.*;
import com.edc.erp.presale.service.OrdDisPresaleActivityService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName OrdDisPresaleActivityController
 * @Author ZhangYao
 * @CreateTime 2024/8/23 8:43
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disPresaleActivity")
@Api(value = "ordDisPresaleActivityController", tags = "预售活动接口")
public class OrdDisPresaleActivityController {

    private final OrdDisPresaleActivityService ordDisPresaleActivityService;

    @ApiOperation(value = "保存预售活动", notes = "保存预售活动", httpMethod = "POST")
    @PostMapping("/savePresaleActivity")
    public Response<Long> savePresaleActivity(@RequestBody SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        saveDisPresaleActivityIn.setBizOrgCode(UserUtil.getBizOrgCode());
        saveDisPresaleActivityIn.setLoginUsername(UserUtil.getUserName());
        saveDisPresaleActivityIn.setOrgCode(UserUtil.getOrgCode());
        ordDisPresaleActivityService.beforeCheckSavePresaleActivityData(saveDisPresaleActivityIn);
        Response<OrdDisPresaleActivity> saveResponse = ordDisPresaleActivityService.savePresaleActivity(saveDisPresaleActivityIn);
        return Response.data(saveResponse.getData().getId());
    }

    @ApiOperation(value = "审核预售活动", notes = "审核预售活动", httpMethod = "POST")
    @PostMapping("/auditPresaleActivity")
    public Response<String> auditPresaleActivity(@RequestBody SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        saveDisPresaleActivityIn.setBizOrgCode(UserUtil.getBizOrgCode());
        saveDisPresaleActivityIn.setLoginUsername(UserUtil.getUserName());
        saveDisPresaleActivityIn.setOrgCode(UserUtil.getOrgCode());
        ordDisPresaleActivityService.beforeCheckSavePresaleActivityData(saveDisPresaleActivityIn);
        return ordDisPresaleActivityService.auditPresaleActivityData(saveDisPresaleActivityIn);
    }

    @ApiOperation(value = "作废预售活动", notes = "作废预售活动", httpMethod = "POST")
    @GetMapping("/invalidPresaleActivity")
    public Response<String> invalidPresaleActivity(@RequestParam Long id) {
        OrdDisPresaleActivity presaleActivity = ordDisPresaleActivityService.getOneById(id);
        if (Objects.isNull(presaleActivity)) {
            return Response.error("预售活动不存在");
        }
        if (!(OrdDisPresaleActivityStatusEnum.SUBMITTED.getKey().equals(presaleActivity.getStatus())
                || OrdDisPresaleActivityStatusEnum.APPROVED.getKey().equals(presaleActivity.getStatus()))) {
            return Response.error("预售活动状态不正确");
        }
        ordDisPresaleActivityService.invalidPresaleActivity(presaleActivity);
        return Response.success("作废成功");
    }

    @ApiOperation(value = "中止预售活动", notes = "中止预售活动", httpMethod = "POST")
    @GetMapping("/stopPresaleActivity")
    public Response<String> stopPresaleActivity(@RequestParam Long id) {
        OrdDisPresaleActivity presaleActivity = ordDisPresaleActivityService.getOneById(id);
        if (Objects.isNull(presaleActivity)) {
            return Response.error("预售活动不存在");
        }
        boolean invalidFlag = OrdDisPresaleActivityStatusEnum.EXECUTED.getKey().equals(presaleActivity.getStatus());
        if (!invalidFlag) {
            return Response.error("生效中预售活动可以中止");
        }
        presaleActivity.setUpdater(UserUtil.getUserName());
        ordDisPresaleActivityService.stopPresaleActivity(presaleActivity);
        return Response.success("中止成功");
    }

    @ApiOperation(value = "按门店代码查询已生效预售活动主图列表（轮播图）", notes = "按门店代码查询已生效预售活动主图列表（轮播图）", httpMethod = "GET")
    @GetMapping("/findExecutePresaleActivityImageForApp")
    public Response<List<OrdDisPresaleActivity>> findExecutePresaleActivityImageForApp(@RequestParam String storeCode) {
        List<OrdDisPresaleActivity> ordDisPresaleActivities = ordDisPresaleActivityService.findExecutePresaleActivityImageList(storeCode);
        return Response.data(ordDisPresaleActivities);
    }

    @ApiOperation(value = "按门店代码查询已生效预售活动详情列表", notes = "按门店代码查询已生效预售活动详情列表", httpMethod = "GET")
    @GetMapping("/findPresaleActivityInfoListForApp")
    public Response<List<PresaleActivityInfoForAppOut>> findPresaleActivityInfoListForApp(@RequestParam String storeCode) {
        List<PresaleActivityInfoForAppOut> presaleActivityInfoOuts = ordDisPresaleActivityService.findPresaleActivityInfoListForApp(storeCode);
        return Response.data(presaleActivityInfoOuts);
    }

    @ApiOperation(value = "分页查询预售活动列表", notes = "分页查询预售活动列表", httpMethod = "POST")
    @PostMapping("/findPresaleActivityList")
    public Response<Page<PresaleActivityListOut>> findPresaleActivityList(@RequestBody PresaleActivityListPageIn presaleActivityListIn) {
        presaleActivityListIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<PresaleActivityListOut> page = ordDisPresaleActivityService.findPresaleActivityListByPage(presaleActivityListIn);
        return Response.data(page);
    }

    @ApiOperation(value = "按id查询预售活动详情", notes = "按id查询预售活动详情", httpMethod = "GET")
    @GetMapping("/findPresaleActivityById")
    public Response<PresaleActivityDetailOut> findPresaleActivityById(@RequestParam Long id) {
        PresaleActivityDetailOut ordDisPresaleActivity = ordDisPresaleActivityService.findPresaleActivityById(id);
        return Response.data(ordDisPresaleActivity);
    }

    @ApiOperation(value = "导入门店", notes = "导入门店")
    @GetMapping("/importPresaleActivityStore")
    public Response<List<ImportPresaleActivityStoreOut>> importPresaleActivityStore(@RequestParam String fileId) {
        return ordDisPresaleActivityService.importPresaleActivityStore(fileId);
    }

    @ApiOperation(value = "延长预售活动订货结束时间", notes = "延长预售活动订货结束时间")
    @PostMapping("/extendEndOrderDate")
    public Response<String> extendEndOrderDate(@RequestBody @Valid ExtendOrderDateIn extendOrderDateIn) {
        Response<CheckExtendEndOrderDateOut> response = ordDisPresaleActivityService.checkExtendEndOrderDate(extendOrderDateIn, UserUtil.getUserName());
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        return ordDisPresaleActivityService.extendEndOrderDate(response.getData());
    }
}
