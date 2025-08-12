package com.edc.erp.disrequestorder.controller;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderDtlPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderDetailOut;
import com.edc.erp.disrequestorder.model.out.DisRequestSummarizingOut;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDetailService;
import com.edc.plugins.common.model.page.Page;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


/**
 * <p>
 * 集货单明细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDelivRequestDetail")
@Api(value = "ordDisDelivRequestDetail", tags = "集货单明细表模块")
public class OrdDisDelivRequestDetailController {

    private final OrdDisDelivRequestDetailService ordDisDelivRequestDetailService;

    @ApiOperation(value = "查询集货单明细")
    @PostMapping("/findRequestOrderDetailList")
    public Response<Page<BackRequestOrderDetailOut>> findRequestOrderDetailList(@RequestBody @Valid BackQueryRequestOrderDtlPageIn dtlPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        dtlPageIn.setBizOrgCode(bizOrgCode);
        return Response.data(ordDisDelivRequestDetailService.findRequestOrderDetailList(dtlPageIn));
    }
    @ApiOperation(value = "运营端导出集货单明细")
    @PostMapping("/exportDetailList")
    public Response<String> exportDetailList(@RequestBody @Valid BackQueryRequestOrderDtlPageIn dtlPageIn, HttpServletRequest request) {
        dtlPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        dtlPageIn.setPageSize(0);
        dtlPageIn.setPageNum(0);
        log.info("开始执行/ord/ordDisDelivRequestDetail/exportDetailList");
        String result = ordDisDelivRequestDetailService.exportDetailList(dtlPageIn);
        log.info("结束执行/ord/ordDisDelivRequestDetail/exportDetailList");
        return Response.data(result);
    }
}
