package com.edc.erp.directly.distribution.controller;

import com.edc.erp.common.handle.EmpowerGroupHandle;
import com.edc.erp.common.model.out.EmpowerGroupBaseDetailOut;
import com.edc.erp.common.model.out.goods.GoodsSortNodeOut;
import com.edc.erp.directly.distribution.job.DirOrderAllocationPoolScheduler;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.in.UpdateAllocationPoolIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolHistoryService;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.DateUtils;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrderAllocationPoolController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 17:46
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/ord/orderAllocationPool")
@Api(value = "orderAllocationPool", tags = "订单调配台控制器")
public class OrderAllocationPoolController {

    private final OrdDirOrderAllocationPoolService ordDirOrderAllocationPoolService;

    private final OrdDirOrderAllocationPoolHistoryService ordDirOrderAllocationPoolHistoryService;

    private final DirOrderAllocationPoolScheduler dirOrderAllocationPoolScheduler;

    private final EmpowerGroupHandle empowerGroupHandle;

    @ApiOperation(value = "批量导入调配明细", notes = "批量导入调配明细", httpMethod = "GET")
    @GetMapping("/importAllocationPoolDetail")
    public Response<String> importAllocationPoolDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId) {
        return ordDirOrderAllocationPoolService.importAllocationPoolDetail(fileId, UserUtil.getUserName());
    }

    @ApiOperation(value = "批量更新", notes = "批量更新", httpMethod = "POST")
    @PostMapping("/batchUpdate")
    public Response<String> batchUpdate(@RequestBody List<UpdateAllocationPoolIn> updateAllocationPoolInList) {
        ordDirOrderAllocationPoolService.batchUpdate(updateAllocationPoolInList, UserUtil.getUserName());
        return Response.success("处理成功");
    }

    @ApiOperation(value = "分页查询", notes = "分页查询", httpMethod = "GET")
    @GetMapping("/findPage")
    public Response<Page<OrdDirOrderAllocationPoolPageOut>> findPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        queryAllocationPoolPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDirOrderAllocationPoolPageOut> page = ordDirOrderAllocationPoolService.findListForPage(queryAllocationPoolPageIn);
        return Response.data(page);
    }

    @ApiOperation(value = "分页查询调配历史", notes = "分页查询调配历史", httpMethod = "GET")
    @GetMapping("/findHistoryPage")
    public Response<Page<OrdDirOrderAllocationPoolPageOut>> findHistoryPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        queryAllocationPoolPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDirOrderAllocationPoolPageOut> page = ordDirOrderAllocationPoolHistoryService.findHistoryListForPage(queryAllocationPoolPageIn);
        return Response.data(page);
    }

    @ApiOperation(value = "导出调配", notes = "导出调配", httpMethod = "GET")
    @GetMapping("/exportAllocationPool")
    public Response<String> exportAllocationPool(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
//        if (StringUtils.isBlank(queryAllocationPoolPageIn.getStoreCode()) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea())) {
//            return Response.error("必须按单店或者区域导出");
//        }
        queryAllocationPoolPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        String url = ordDirOrderAllocationPoolService.exportAllocationPool(queryAllocationPoolPageIn);
        return Response.data(url);
    }

    @ApiOperation(value = "导出调配历史", notes = "导出调配历史", httpMethod = "GET")
    @GetMapping("/exportAllocationPoolHistory")
    public Response<String> exportAllocationPoolHistory(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        boolean truncationDateTimeFlag = StringUtils.isBlank(queryAllocationPoolPageIn.getTruncationDateTimeBegin()) && StringUtils.isBlank(queryAllocationPoolPageIn.getTruncationDateTimeEnd());
        if (StringUtils.isBlank(queryAllocationPoolPageIn.getStoreCode()) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea()) && truncationDateTimeFlag && truncationDateTimeFlag) {
            return Response.error("必须按单店或者区域或者选择截单时间导出");
        }
        if (StringUtils.isBlank(queryAllocationPoolPageIn.getStoreCode()) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea()) && !truncationDateTimeFlag) {
            Duration duration = Duration.between(DateUtils.parseTime(queryAllocationPoolPageIn.getTruncationDateTimeBegin()), DateUtils.parseTime(queryAllocationPoolPageIn.getTruncationDateTimeEnd()));
            if (duration.toDays() > 3) {
                return Response.error("单独按截单时间导出最大支持3天内数据");
            }
        }
        queryAllocationPoolPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        String url = ordDirOrderAllocationPoolHistoryService.exportAllocationPoolHistory(queryAllocationPoolPageIn);
        return Response.data(url);
    }

    @ApiOperation(value = "手动触发计算补单量任务", notes = "手动触发计算补单量任务", httpMethod = "GET")
    @GetMapping("/dirHandleSupplementQuantityJob")
    public Response<String> dirHandleSupplementQuantityJob() {
        dirOrderAllocationPoolScheduler.dirHandleSupplementQuantityJob();
        return Response.success();
    }

    @ApiOperation(value = "手动触发调配创建订货单", notes = "手动触发调配创建订货单", httpMethod = "GET")
    @GetMapping("/dirAllocationPoolCreateOrderJob")
    public Response<String> dirAllocationPoolCreateOrderJob() {
        dirOrderAllocationPoolScheduler.dirAllocationPoolCreateOrderJob();
        return Response.success();
    }

    @ApiOperation(value = "根据权限组和业务组织获取组明细集合", notes = "根据权限组和业务组织获取组明细集合", httpMethod = "GET")
    @GetMapping("/findEmpowerGroupsInfoByType")
    public Response<List<EmpowerGroupBaseDetailOut>> findEmpowerGroupsInfoByType(@RequestParam String groupType) {
        List<EmpowerGroupBaseDetailOut> list = empowerGroupHandle.findEmpowerGroupsInfoByType(UserUtil.getBizOrgCode(), groupType);
        return Response.data(list);
    }

    @ApiOperation(value = "根据组织查询直营调配池当下商品所有大中小类", notes = "根据组织查询直营调配池当下商品所有大中小类", httpMethod = "GET")
    @GetMapping("/findAllocationPoolSortTree")
    public Response<GoodsSortNodeOut> findAllocationPoolSortTree(String bizOrgCode) {
        if(StringUtils.isBlank(bizOrgCode)){
            bizOrgCode = UserUtil.getBizOrgCode();
        }
        GoodsSortNodeOut rootNode = ordDirOrderAllocationPoolService.findAllocationPoolSortTree(bizOrgCode);
        return Response.data(rootNode);
    }
}
