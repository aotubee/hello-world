package com.edc.erp.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.service.DisOrderPayService;
import com.edc.erp.presale.model.in.UpdatePresaleOrderPaidForMqIn;
import com.edc.erp.presale.service.OrdDisPresaleOrderService;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/ord/disPay")
@Api(value = "payController", tags = "配销支付接口")
public class PayController {

    @Autowired
    private DisOrderPayService disOrderPayService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private OrdDisPresaleOrderService ordDisPresaleOrderService;

    @Autowired
    private RedisService redisService;

    @ApiOperation(value = "配销单批量支付")
    @PostMapping(value = "/disOrderPay")
    public Response<String> disOrderPay(@RequestBody List<Long> orderIdList) {
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(UserUtil.getUserName());
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        return disOrderPayService.disOrderPay(orderIdList, appUserOut);
    }

    @ApiOperation(value = "配销单批量支付")
    @PostMapping(value = "/t")
    public Response<String> t(@RequestBody JSONObject jsonObject) {
        UpdatePresaleOrderPaidForMqIn updatePresaleOrderPaidForMqIn = JSONObject.toJavaObject(jsonObject, UpdatePresaleOrderPaidForMqIn.class);
        try {
            ordDisPresaleOrderService.updatePresaleOrderAfterPaidSuccess(updatePresaleOrderPaidForMqIn.getPresaleOrderId(), updatePresaleOrderPaidForMqIn.getLoginUsername());
        } catch (Exception e) {
            log.error("123", e);
        }
        return Response.success();
    }

    @ApiOperation(value = "预售订单支付")
    @PostMapping(value = "/disPresaleOrderPay")
    public Response<String> disPresaleOrderPay(@RequestParam Long id) {
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(UserUtil.getUserName());
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        String key = DisSystemConstant.DIS_PRESALE_ORDER_PAY + appUserOut.getBizOrgCode() + SystemConstant.COLON + id;
        if (!redisService.setIfAbsent(key, id, 1L, TimeUnit.MINUTES)) {
            log.error("配销预售订单ID{}已进入支付处理流程", id);
            return Response.error("请勿重复支付");
        }
        Response<String> response = ordDisPresaleOrderService.payPresaleOrder(id, appUserOut, UserUtil.getUserName());
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        return Response.success("支付成功");
    }

    @GetMapping(value = "/d")
    public Response<String> d(@RequestParam Long id) {
        UpdatePresaleOrderPaidForMqIn updatePresaleOrderPaidForMqIn = new UpdatePresaleOrderPaidForMqIn();
        updatePresaleOrderPaidForMqIn.setLoginUsername("aa");
        updatePresaleOrderPaidForMqIn.setPresaleOrderId(id);
        ordDisPresaleOrderService.updatePresaleOrderAfterPaidSuccess(updatePresaleOrderPaidForMqIn.getPresaleOrderId(), updatePresaleOrderPaidForMqIn.getLoginUsername());
        return Response.success();
    }



}
