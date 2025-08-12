package com.edc.erp.directly.distribution.controller;

import cn.hutool.core.util.StrUtil;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderDetailHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.in.OrderDetailIn;
import com.edc.erp.directly.distribution.model.out.BackHeaderOrderDetailOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailOut;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderLogEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.DateUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 订货单详细表 前端控制器
 *
 * @author wanglidong
 * @since 2022-11-16 09:21:49
 */
@Slf4j
@RestController
@RequestMapping("/ord/ordDirOrderDetail")
@Api(value = "ordDirOrderDetail", tags = "订货单详细表模块")
public class OrdDirOrderDetailController {

    @Autowired
    private OrderHandle orderHandle;
    @Autowired
    private OrderDetailHandle orderDetailHandle;
    @Autowired
    private DirOrderConfigHandle orderConfigHandle;
    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;
    @Autowired
    private AsyncLogService asyncLogService;

    @ApiOperation(value = "直营订货单明细列表查询")
    @GetMapping(value = "/findByPage")
    public Response<Page<OrderDetailOut>> findOrderDetailOutPage(OrderDetailIn orderDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderDetailIn.setIsGift(NumberUtil.INTEGER_ZERO);
        Page<OrderDetailOut> orderOutPage = orderDetailHandle.findOrderDetailOutPage(orderDetailIn, bizOrgCode);
        return Response.data(orderOutPage);
    }

    @ApiOperation(value = "导出直营订货单详情", notes = "导出直营订货单详情")
    @GetMapping("/exportOrdDirOrderDetail")
    public Response exportOrdDirOrderDetail(OrderDetailIn orderDetailIn) {
        log.info("开始执行/ord/ordDirOrderDetail/exportOrdDirOrderDetail");
        String privateUrl = orderDetailHandle.exportOrdDirOrderDetail(orderDetailIn);
        log.info("结束执行/ord/ordDirOrderDetail/exportOrdDirOrderDetail");
        return StringUtils.isBlank(privateUrl) ? Response.error("导出失败，请重试") : Response.data(privateUrl);
    }

    /**
     * 运营端直营订货单作废
     *
     * @param orderId
     * @return
     */
    @ApiOperation(value = "直营订货单作废")
    @GetMapping(value = "/backInvalid")
    public Response<String> backInvalidOrder(@RequestParam("orderId") Long orderId) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        OrdDirOrder orderOut = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (orderOut == null) {
            return Response.error("订货单不存在");
        }
        String statusCode = orderOut.getOrderStatusCode();
        if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
            return Response.error("已转单的订货单不可作废");
        }
        if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
            return Response.error("不可重复作废");
        }
        orderDetailHandle.invalidOrder(orderOut, userName, bizOrgCode);

        // 手动作废整单推送订单追踪日志
        String content = MessageFormat.format(OrderLogEnum.OPERATE_ORDER_INVALID.getKey(), orderOut.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), orderId.toString(),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), userName);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("已作废");
    }

    @ApiOperation(value = "加推直营订货单", notes = "加推直营订货单")
    @GetMapping("/addPushOrder")
    public Response addPushOrder(@RequestParam("orderId") Long orderId, @RequestParam(value = "bizOrgCode", required = false) String bizOrgCode) {
        if (StrUtil.isBlank(bizOrgCode)) {
            bizOrgCode = UserUtil.getBizOrgCode();
        }
        OrdDirOrder ordDisOrder = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (Objects.isNull(ordDisOrder)) {
            return Response.error("无效订货单");
        }
        OrdDirOrderCycle ordDisOrderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDisOrder.getOrderCycleId(), bizOrgCode);
        DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(ordDisOrderCycle.getOrderTypeConfigId(), bizOrgCode);
        if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(dirOrderTypeConfig.getOrderPeriod())) {
            LocalDateTime beginTime = DateUtils.parseTime(LocalDate.now() + " 09:30:00");
            LocalDateTime endTime = DateUtils.parseTime(LocalDate.now() + " 12:00:00");
            if (LocalDateTime.now().isAfter(beginTime) && LocalDateTime.now().isBefore(endTime)) {
                log.warn("低温订货单09:30~12:00不允许加推");
            }
        }
        String userName = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
        return orderDetailHandle.addPushOrder(ordDisOrder, userName);
    }

    @ApiOperation(value = "查询直营订货单明细表头")
    @GetMapping("/getBackHeader")
    public Response<BackHeaderOrderDetailOut> getBackHeaderOrderDetailOut(@RequestParam("orderId") @Valid Long orderId) {
        if (null == orderId) {
            return Response.error("订货单主键不能为空");
        }
        BackHeaderOrderDetailOut backHeaderOrderDetailOut = orderDetailHandle.getBackHeaderOrderDetailOut(orderId, UserUtil.getBizOrgCode());
        return Response.data(backHeaderOrderDetailOut);
    }

    /**
     * 批量加推订货单
     *
     * @param orderIds
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "批量加推订货单")
    @GetMapping("/batchAddPushOrder")
    public Response batchAddPushOrder(@RequestParam("orderIds") List<Long> orderIds,
                                      @RequestParam(value = "bizOrgCode", required = false) String bizOrgCode) {
        if (StringUtils.isBlank(bizOrgCode)) {
            bizOrgCode = UserUtil.getBizOrgCode();
        }
        StringBuilder msgBuilder = new StringBuilder();
        for (Long orderId : orderIds) {
            OrdDirOrder ordDirOrder = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
            if (Objects.isNull(ordDirOrder)) {
                msgBuilder.append(orderId).append("无效订货单;");
                continue;
            }
            OrdDirOrderCycle ordDirOrderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDirOrder.getOrderCycleId(), bizOrgCode);
            DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(ordDirOrderCycle.getOrderTypeConfigId(), bizOrgCode);
            if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(dirOrderTypeConfig.getOrderPeriod())) {
                LocalDateTime beginTime = DateUtils.parseTime(LocalDate.now() + " 09:30:00");
                LocalDateTime endTime = DateUtils.parseTime(LocalDate.now() + " 12:00:00");
                if (LocalDateTime.now().isAfter(beginTime) && LocalDateTime.now().isBefore(endTime)) {
                    log.warn("低温订货单09:30~12:00不允许加推");
                }
            }
            Response response;
            try {
                String userName = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
                response = orderDetailHandle.addPushOrder(ordDirOrder, userName);
                if (!response.isSuccess()) {
                    msgBuilder.append(ordDirOrder.getOrderNo()).append(response.getMessage()).append(";");
                }
            } catch (Exception e) {
                msgBuilder.append(ordDirOrder.getOrderNo()).append(e.getMessage()).append(";");
            }
        }
        return msgBuilder.length() > 0 ? Response.error(msgBuilder.toString()) : Response.success("加推成功");
    }

    /**
     * 运营端批量直营订货单作废
     *
     * @param orderIds
     * @return
     */
    @ApiOperation(value = "运营端批量直营订货单作废")
    @GetMapping(value = "/batchBackInvalid")
    public Response<String> batchBackInvalidOrder(@RequestParam("orderIds") List<Long> orderIds) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        StringBuilder msgBuilder = new StringBuilder();
        for (Long orderId : orderIds) {
            OrdDirOrder orderOut = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
            if (orderOut == null) {
                msgBuilder.append(orderId).append("订货单不存在;");
                continue;
            }
            String statusCode = orderOut.getOrderStatusCode();
            if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
                msgBuilder.append(orderOut.getOrderNo()).append("已转单的订货单不可作废;");
                continue;
            }
            if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
                msgBuilder.append(orderOut.getOrderNo()).append("不可重复作废;");
                continue;
            }
            orderDetailHandle.invalidOrder(orderOut, userName, bizOrgCode);
            // 手动作废整单推送订单追踪日志
            String content = MessageFormat.format(OrderLogEnum.OPERATE_ORDER_INVALID.getKey(), orderOut.getOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), orderId.toString(),
                    OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), userName);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return msgBuilder.length() > 0 ? Response.error(msgBuilder.toString()) : Response.success("作废成功");
    }
}
