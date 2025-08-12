package com.edc.erp.directly.dirrequestorder.controller;

import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderDtlPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderDetailOut;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDetailService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * <p>
 * 要货单明细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivRequestDetail")
@Api(value = "ordDirDelivRequestDetail", tags = "要货单明细表模块")
public class OrdDirDelivRequestDetailController {

    private final OrdDirDelivRequestDetailService ordDirDelivRequestDetailService;

    @ApiOperation(value = "查询要货单明细")
    @PostMapping("/findRequestOrderDetailList")
    public Response<Page<DirRequestOrderDetailOut>> findRequestOrderDetailList(@RequestBody @Valid DirRequestOrderDtlPageIn dtlPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        dtlPageIn.setBizOrgCode(bizOrgCode);
        return Response.data(ordDirDelivRequestDetailService.findRequestOrderDetailList(dtlPageIn));
    }

    @ApiOperation(value = "运营端导出要货单明细")
    @PostMapping("/exportDetailList")
    public Response<String> exportDetailList(@RequestBody @Valid DirRequestOrderDtlPageIn dtlPageIn) {
        dtlPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        dtlPageIn.setPageSize(0);
        dtlPageIn.setPageNum(0);
        log.info("开始执行/ord/ordDirDelivRequestDetail/exportDetailList");
        String result = ordDirDelivRequestDetailService.exportDetailList(dtlPageIn);
        log.info("结束执行/ord/ordDirDelivRequestDetail/exportDetailList");
        return Response.data(result);
    }

}
