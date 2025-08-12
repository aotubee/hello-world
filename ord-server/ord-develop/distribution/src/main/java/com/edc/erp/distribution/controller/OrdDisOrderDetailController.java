package com.edc.erp.distribution.controller;

import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.handle.DisOrderDetailHandle;
import com.edc.erp.distribution.model.in.OrderDetailIn;
import com.edc.erp.distribution.model.out.BackHeaderOrderDetailOut;
import com.edc.erp.distribution.model.out.OrderDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 配销订货单详细表 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderDetail")
@Api(value = "ordDisOrderDetail", tags = "配销订货单详细表模块")
public class OrdDisOrderDetailController {

    private final DisOrderDetailHandle orderDetailHandle;


    /**
     * 订货单明细列表查询
     *
     * @param orderDetailIn
     * @return
     */
    @ApiOperation(value = "订货单明细列表查询")
    @GetMapping(value = "/findByPage")
    public Response<Page<OrderDetailOut>> findOrderDetailOutPage(OrderDetailIn orderDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderDetailIn.setIsGift(NumberUtil.INTEGER_ZERO);
        Page<OrderDetailOut> orderOutPage = orderDetailHandle.findOrderDetailOutPage(orderDetailIn, bizOrgCode);
        return Response.data(orderOutPage);
    }

    /**
     * 运营端查询订货单明细表头
     *
     * @param orderId
     * @return
     */
    @ApiOperation(value = "运营端查询订货单明细表头")
    @GetMapping("/getBackHeader")
    public Response<BackHeaderOrderDetailOut> getBackHeaderOrderDetailOut(@RequestParam("orderId") Long orderId) {
        if (null == orderId) {
            return Response.error("订货单主键不能为空");
        }
        String bizOrgCode = UserUtil.getBizOrgCode();
        BackHeaderOrderDetailOut backHeaderOrderDetailOut = orderDetailHandle.getBackHeaderOrderDetailOut(orderId, bizOrgCode);
        return Response.data(backHeaderOrderDetailOut);
    }

    /**
     * 订货单明细列表导出
     *
     * @param orderDetailIn
     * @return
     */
    @ApiOperation(value = "订货单明细列表导出")
    @GetMapping(value = "/export")
    public Response<String> exportOrderDetailOut(OrderDetailIn orderDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        log.info("开始执行/ord/ordDisOrderDetail/export");
        String privateUrl = orderDetailHandle.exportOrderDetailOut(orderDetailIn, bizOrgCode);
        log.info("执行结束/ord/ordDisOrderDetail/export");
        return StringUtils.isBlank(privateUrl) ? Response.error("导出失败，请重试") : Response.data(privateUrl);
    }
}
