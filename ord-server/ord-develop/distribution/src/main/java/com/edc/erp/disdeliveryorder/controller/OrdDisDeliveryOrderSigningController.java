package com.edc.erp.disdeliveryorder.controller;

import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.model.in.CacheTakeDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.in.DisSignDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderSigningService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;


/**
 * <p>
 * 配销单签收 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDeliveryOrderSigning")
@Api(value = "ordDisDeliveryOrderSigning", tags = "配销单签收模块")
public class OrdDisDeliveryOrderSigningController {

    private final OrdDisDeliveryOrderSigningService ordDisDeliveryOrderSigningService;

    private final OrdDisDeliveryService ordDisDeliveryService;

    @ApiOperation(value = "收货签收", notes = "收货签收", httpMethod = "POST")
    @PostMapping("/signDisDeliveryOrder")
    public Response signDisDeliveryOrder(@RequestBody @Valid DisSignDeliveryOrderIn signDeliveryOrderIn, HttpServletRequest request) {
        OrdDisDelivery deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(signDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配销单");
        }
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配销单状态不正确，不能签收");
        }
        if (NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversalOrder())) {
            return Response.error("配销单已冲销或是冲销单，不能签收");
        }
        return ordDisDeliveryOrderSigningService.signDisDeliveryOrder(signDeliveryOrderIn, deliveryOrder, UserUtil.getUserName(request));
    }

    @ApiOperation(value = "提交签收时收货前高值整件缓存数据", notes = "提交签收时收货前高值整件缓存数据", httpMethod = "POST")
    @PostMapping("/submitBeforeTakeDisDeliveryInfoToCache")
    public Response<String> submitBeforeTakeDisDeliveryInfoToCache(@RequestBody @Valid CacheTakeDisDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        OrdDisDelivery deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(cacheTakeDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配销单");
        }
        if (DeliveryOrderEnum.COMPLETED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配销单已经收货了");
        }
        return ordDisDeliveryOrderSigningService.submitBeforeTakeDisDeliveryInfoToCache(deliveryOrder, cacheTakeDeliveryOrderIn.getCacheTakeDeliveryOrderGoodsInList());
    }

}
