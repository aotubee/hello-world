package com.edc.erp.disfirstorder.controller;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisFirstOrderAuditIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstIn;
import com.edc.erp.disfirstorder.model.out.OrdDerDisOrderFirstOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.upperlowerlimit.job.UpperLowerLimitOrderScheduler;
import com.edc.plugins.common.model.page.Page;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * <p>
 * 配销铺货单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderFirst")
@Api(value = "ordDisOrderFirst", tags = "配销铺货单模块")
public class OrdDisOrderFirstController {

    private final OrdDisOrderFirstService ordDisOrderFirstService;

    private final OrdDisOrderFirstDetailService ordDisOrderFirstDetailService;

    private final AsyncTaskItemService asyncTaskItemService;

    private final StoreCenterService storeCenterService;

    @ApiOperation(value = "获取首单铺货订单列表", notes = "", httpMethod = "GET")
    @GetMapping("/findByPage")
    public Response<Page<OrdDerDisOrderFirstOut>> findFirstOrderPage(OrdDisOrderFirstIn ordDisOrderFirstIn) {
//        ordDisOrderFirstIn.setOrgCode(UserUtil.getOrgCode());
        ordDisOrderFirstIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDerDisOrderFirstOut> firstOrderOutPage = ordDisOrderFirstService.findFirstOrderPage(ordDisOrderFirstIn);
        return Response.data(firstOrderOutPage);
    }

    @ApiOperation(value = "获取首单铺货订单", notes = "", httpMethod = "GET")
    @GetMapping("/get")
    public Response<OrdDerDisOrderFirstOut> getFirstOrderOut(@RequestParam Long ordDisOrderFirstId) {
        OrdDerDisOrderFirstOut orderDisOrderFirstOut = ordDisOrderFirstService.getFirstOrderOut(ordDisOrderFirstId);
        return Response.data(orderDisOrderFirstOut);
    }

//    @ApiOperation(value = "审核首单铺货订单", notes = "", httpMethod = "POST")
//    @PostMapping("/audit")
//    public Response<OrdDisOrderFirst> auditFirstOrder(@RequestBody InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        // 校验明细数据
//        Response response = ordDisOrderFirstDetailService.checkRepeatParam(insertFirstOrderDetailIn, bizOrgCode);
//        if (!response.isSuccess()) {
//            return response;
//        }
//        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.selectByPrimaryKey(insertFirstOrderDetailIn.getFirstOrderId());
//        if (Objects.isNull(ordDisOrderFirst)) {
//            return Response.error("找不到此铺货单");
//        }
//        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
//            return Response.error("只有待审核铺货单可以审核");
//        }
//        return ordDisOrderFirstService.auditFirstOrder(insertFirstOrderDetailIn);
//    }

    @ApiOperation(value = "作废铺货单", notes = "", httpMethod = "POST")
    @PostMapping("/invalid")
    public Response invalidFirstOrder(@RequestBody OrdDisOrderFirst ordDisOrderFirst) {
        OrdDisOrderFirst query = new OrdDisOrderFirst();
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        query.setId(ordDisOrderFirst.getId());
        OrdDisOrderFirst firstOrder = ordDisOrderFirstService.selectOne(query);
        if (firstOrder == null) {
            return Response.error("找不到此铺货单");
        }
        int count = ordDisOrderFirstService.invalidFirstOrder(firstOrder);
        if (count > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    @ApiOperation(value = "校验商品信息", notes = "", httpMethod = "GET")
    @GetMapping("/checkOrderGoods")
    public Response<OrderGoodsOut> checkOrderGoods(@RequestParam String goodsCode, @RequestParam String storeCode) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        return ordDisOrderFirstService.checkOrderGoods(goodsCode, storeCode, bizOrgCode);

    }

    @PostMapping("/disFirstToDelivery")
    @ApiOperation(value = "铺货单转配销单", notes = "铺货单转配销单")
    public void disFirstToDelivery(@RequestBody JSONObject json) {
        OrdDisOrderFirst ordDisOrderFirst = JSONObject.parseObject(json.toJSONString(), OrdDisOrderFirst.class);
        asyncTaskItemService.disFirstToDelivery(ordDisOrderFirst);
    }


    @ApiOperation(value = "批量导入直营铺货单", notes = "批量导入直营铺货单", httpMethod = "GET")
    @GetMapping("/asyncImportFirstOrderDetail")
    public Response<Long> asyncImportFirstOrderDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId,
            @ApiParam(name = "铺货单id", value = "firstOrderId") @RequestParam(value = "firstOrderId", required = false) Long firstOrderId,
            @ApiParam(name = "是否立即生效", value = "isEffectiveImmediately") @RequestParam(value = "isEffectiveImmediately") Integer isEffectiveImmediately,
            @ApiParam(name = "生效时间", value = "effectiveTime") @RequestParam(value = "effectiveTime", required = false) LocalDateTime effectiveTime,
            @ApiParam(name = "门店代码", value = "storeCode") @RequestParam(value = "storeCode") String storeCode) {
        StoreOut storeOut = null;
        if (Objects.isNull(firstOrderId)) {
            if (StringUtils.isBlank(storeCode)) {
                return Response.error("请指定门店");
            } else {
                storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
                if (Objects.isNull(storeOut)) {
                    return Response.error("门店不存在");
                }
            }
        }
        OrdDisOrderFirst ordDirOrderFirst = ordDisOrderFirstService.getOrdDisOrderFirstForImport(firstOrderId, storeOut, UserUtil.getUserName(), isEffectiveImmediately, effectiveTime);
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以新增明细");
        }
        return ordDisOrderFirstService.asyncImportFirstOrderDetail(fileId, ordDirOrderFirst, UserUtil.getUserName());
    }

    @ApiOperation(value = "审核铺货单", notes = "审核铺货单", httpMethod = "POST")
    @PostMapping("/auditFirstOrder")
    public Response<String> auditFirstOrder(@RequestBody OrdDisFirstOrderAuditIn ordDisFirstOrderAuditIn) {
        String loginUsername = UserUtil.getUserName();
        return ordDisOrderFirstService.auditFirstOrder(ordDisFirstOrderAuditIn, loginUsername);
    }


}
