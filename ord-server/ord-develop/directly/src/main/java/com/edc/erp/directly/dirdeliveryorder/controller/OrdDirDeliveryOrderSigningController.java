package com.edc.erp.directly.dirdeliveryorder.controller;

import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.model.in.CacheTakeDirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirSignDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderSigningService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
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
 * 配货单签收 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDeliveryOrderSigning")
@Api(value = "ordDirDeliveryOrderSigning", tags = "配货单签收模块")
public class OrdDirDeliveryOrderSigningController {

    private final OrdDirDeliveryOrderSigningService ordDirDeliveryOrderSigningService;

    private final OrdDirDeliveryService ordDirDeliveryService;

    @ApiOperation(value = "收货签收", notes = "收货签收", httpMethod = "POST")
    @PostMapping("/signDirDeliveryOrder")
    public Response signDirDeliveryOrder(@RequestBody @Valid DirSignDeliveryOrderIn signDeliveryOrderIn, HttpServletRequest request) {
        OrdDirDelivery deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(signDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配货单状态不正确，不能签收");
        }
        if (NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversalOrder())) {
            return Response.error("配货单已冲销或是冲销单，不能签收");
        }
        return ordDirDeliveryOrderSigningService.signDirDeliveryOrder(signDeliveryOrderIn, deliveryOrder, UserUtil.getUserName(request));
    }

    @ApiOperation(value = "提交签收时收货前高值整件缓存数据", notes = "提交签收时收货前高值整件缓存数据", httpMethod = "POST")
    @PostMapping("/submitBeforeTakeDirDeliveryInfoToCache")
    public Response<String> submitBeforeTakeDirDeliveryInfoToCache(@RequestBody @Valid CacheTakeDirDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        OrdDirDelivery deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(cacheTakeDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        if (DeliveryOrderEnum.COMPLETED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配货单已经收货了");
        }
        return ordDirDeliveryOrderSigningService.submitBeforeTakeDirDeliveryInfoToCache(deliveryOrder, cacheTakeDeliveryOrderIn.getCacheTakeDeliveryOrderGoodsInList());
    }

}
