package com.edc.erp.ord.controller;

import com.edc.erp.common.service.UnificationOrderService;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.dts.model.order.vo.*;
import com.edc.sdk.dts.util.DtsOrdResultInterface;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yaojinpeng
 * @since 2022/11/1 12:07
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/dtsCallBack")
@Api(value = "DTS回传", tags = "DTS回传")
public class DtsCallBackController implements DtsOrdResultInterface {

    @Autowired
    private UnificationOrderService unificationOrderService;

    @ApiOperation(value = "配销单DTS回传",notes = "配销单DTS回传")
    @PostMapping("/unificationOrderCallBack")
    @Override
    public Response<String> unificationOrderCallBack(@RequestBody @Valid UnificationBillVO unificationBillVO) {
        return unificationOrderService.unificationOrderCallBack(unificationBillVO);
    }

    @ApiOperation(value = "退货单DTS回传(收货)",notes = "退货单DTS回传(收货)")
    @PostMapping("/unificationReOrderCallBack")
    @Override
    public Response<String> unificationReOrderCallBack(@RequestBody @Valid UnificationReBillVO unificationReBillVO) {
        return unificationOrderService.unificationReOrderCallBack(unificationReBillVO);
    }

    @ApiOperation(value = "批发出货单DTS回传",notes = "批发出货单DTS回传")
    @PostMapping("/wholesaleOrderCallBack")
    @Override
    public Response<String> wholesaleOrderCallBack(@RequestBody @Valid WholesaleBillVO wholesaleBillVO) {
        return unificationOrderService.wholesaleOrderCallBack(wholesaleBillVO);
    }

    @ApiOperation(value = "批发退货单DTS回传",notes = "批发退货单DTS回传")
    @PostMapping("/wholesaleReOrderCallBack")
    @Override
    public Response<String> wholesaleReOrderCallBack(@RequestBody @Valid WholesaleReBillVO wholesaleReBillVO) {
        return unificationOrderService.wholesaleReOrderCallBack(wholesaleReBillVO);
    }

    @ApiOperation(value = "差异单DTS回传",notes = "差异单DTS回传")
    @PostMapping("/differenceOrderCallBack")
    @Override
    public Response<String> differenceOrderCallBack(@RequestBody @Valid DifferenceBillVO differenceBillVO) {
        return unificationOrderService.differenceOrderCallBack(differenceBillVO);
    }
}
