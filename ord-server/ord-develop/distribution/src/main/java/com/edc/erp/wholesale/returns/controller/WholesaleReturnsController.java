package com.edc.erp.wholesale.returns.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.wholesale.model.in.returns.WholesaleRetReceivingIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsDetailIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsListIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnsListOut;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.dts.model.order.vo.WholesaleReBillVO;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * <p>
 * 批发退货单 前端控制器
 * </p>
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/wholesaleReturns")
@Api(value = "wholesaleReturns", tags = "批发退货单模块")
public class WholesaleReturnsController {

    @Autowired
    private WholesaleReturnsService wholesaleReturnsService;

    @Autowired
    private AsyncTaskItemService asyncTaskItemService;


    @ApiOperation(value = "批发退货单列表查询", notes = "批发退货单列表查询")
    @PostMapping("/findByPage")
    public Response<Page<WholesaleReturnsListOut>> findWholesaleReturnsForPage(@RequestBody WholesaleReturnsListIn wholesaleReturnsListIn) {
        List<WholesaleReturnsListOut> wholesaleReturnsListOutList = wholesaleReturnsService.findWholesaleReturnsForPage(wholesaleReturnsListIn);
        Page page = new Page<>(wholesaleReturnsListIn);
        page.setList(wholesaleReturnsListOutList);
        return Response.data(page);
    }

    @ApiOperation(value = "根据批发退货单单号查询退货单详情", notes = "根据批发退货单单号查询退货单详情")
    @GetMapping("/getDetailByWholesaleReturnNo")
    public Response<WholesaleReturnAndDetailOut> getDetailByWholesaleReturnNo(@RequestParam("wholesaleReturnNo") @ApiParam(name = "批发退货单单号", value = "wholesaleReturnNo", required = true) String wholesaleReturnNo) {
        WholesaleReturnAndDetailOut data = wholesaleReturnsService.getDetailByWholesaleReturnNo(wholesaleReturnNo);
        return Response.data(data);
    }

    @ApiOperation(value = "保存批发退货单", notes = "保存批发退货单")
    @PostMapping("/save")
    public Response saveWholesaleReturnsDetail(@RequestBody WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        if (CollectionUtil.isEmpty(wholesaleReturnsDetailIn.getWholesaleReturnDetailList())) {
            return Response.error("请至少录入一条退货单明细！");
        }
        if (StrUtil.isBlank(wholesaleReturnsDetailIn.getWholesaleReturns().getClientCode())) {
            throw new BusinessException("客户代码不能为空");
        }
        wholesaleReturnsDetailIn.getWholesaleReturns().setBizOrgCode(UserUtil.getBizOrgCode());
        wholesaleReturnsDetailIn.getWholesaleReturns().setOrgCode(UserUtil.getOrgCode());
        Response response = wholesaleReturnsService.saveWholesaleReturnsDetail(wholesaleReturnsDetailIn);
        if (response.isSuccess()) {
            return response;
        }
        return Response.error(response.getMessage());
    }

    /**
     * 批发退货单DTS回传 手动机制
     * @param json 发出货单DTS回传入参
     */
    @PostMapping("/disWholesaleReDtsToErp")
    @ApiOperation(value = "批发退货单DTS回传手动机制", notes = "批发退货单DTS回传手动机制")
    public Response<String> disWholesaleReDtsToErp(@RequestBody JSONObject json) {
        WholesaleReBillVO wholesaleReBillVO = JSON.parseObject(json.toJSONString(), WholesaleReBillVO.class);
        asyncTaskItemService.wholesaleReOrderCallBack(wholesaleReBillVO);
        return Response.success();
    }

    /**
     * 批发退-手动收货
     * @param wholesaleRetReceivingIn 批发退-手动收货 入参
     * @return
     */
    @ApiOperation(value = "批发退-手动收货", notes = "批发退-手动收货")
    @PostMapping("/receiving")
    public Response<String> wholesaleReturnsReceiving(
            @RequestBody @Valid WholesaleRetReceivingIn wholesaleRetReceivingIn) {

        //业务组织
        wholesaleRetReceivingIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return wholesaleReturnsService.wholesaleReturnsReceiving(wholesaleRetReceivingIn);
    }

    /**
     * 批量导出批发退货单
     * @param wholesaleReturnsListIn 批发出货单查询入参
     * @return
     */
    @ApiOperation(value = "批量导出批发退货单", notes = "批量导出批发退货单")
    @PostMapping("/exportWholesaleReturnOrder")
    public Response<String> exportWholesaleReturnOrder(@RequestBody WholesaleReturnsListIn wholesaleReturnsListIn) {
        //业务组织
        wholesaleReturnsListIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(wholesaleReturnsService.exportWholesaleReturnOrder(wholesaleReturnsListIn));
    }

    @ApiOperation(value = "统计列表查询结果汇总数据", notes = "统计列表查询结果汇总数据")
    @PostMapping("/sumWholesaleReturnDateInfo")
    public Response<WholesaleReturnDateInfoOut> sumWholesaleReturnDateInfo(@RequestBody WholesaleReturnsListIn wholesaleReturnsListIn) {
        wholesaleReturnsListIn.setBizOrgCode(UserUtil.getBizOrgCode());
        WholesaleReturnDateInfoOut wholesaleReturnDateInfoOut = wholesaleReturnsService.sumWholesaleReturnDateInfo(wholesaleReturnsListIn);
        return Response.data(wholesaleReturnDateInfoOut);
    }

}
