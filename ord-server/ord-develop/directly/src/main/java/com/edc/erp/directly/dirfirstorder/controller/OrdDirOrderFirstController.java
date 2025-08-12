package com.edc.erp.directly.dirfirstorder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirFirstOrderAuditIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstIn;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDerDirOrderFirstOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstService;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * <p>
 * 铺货单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderFirst")
@Api(value = "ordDirOrderFirst", tags = "铺货单模块")
public class OrdDirOrderFirstController {

    private final OrdDirOrderFirstService ordDirOrderFirstService;

    private final DirAsyncTaskItemService dirAsyncTaskItemService;

    private final StoreCenterService storeCenterService;

    @ApiOperation(value = "获取首单铺货订单列表", notes = "", httpMethod = "GET")
    @PostMapping("/findByPage")
    public Response<Page<OrdDerDirOrderFirstOut>> findDirFirstOrderPage(@RequestBody OrdDirOrderFirstIn ordDirOrderFirstIn) {
//        ordDirOrderFirstIn.setOrgCode(UserUtil.getOrgCode());
        ordDirOrderFirstIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDerDirOrderFirstOut> firstOrderOutPage = ordDirOrderFirstService.findFirstOrderPage(ordDirOrderFirstIn);
        return Response.data(firstOrderOutPage);
    }

    @ApiOperation(value = "获取首单铺货订单", notes = "", httpMethod = "GET")
    @GetMapping("/get")
    public Response<OrdDerDirOrderFirstOut> getDirFirstOrderOut(@RequestParam Long ordDirOrderFirstId) {
        OrdDerDirOrderFirstOut orderDirOrderFirstOut = ordDirOrderFirstService.getFirstOrderOut(ordDirOrderFirstId);
        return Response.data(orderDirOrderFirstOut);
    }

    @ApiOperation(value = "作废铺货单", notes = "", httpMethod = "POST")
    @PostMapping("/invalid")
    public Response invalidDirFirstOrder(@RequestBody OrdDirOrderFirst ordDirOrderFirst) {
        OrdDirOrderFirst query = new OrdDirOrderFirst();
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        query.setId(ordDirOrderFirst.getId());
        OrdDirOrderFirst firstOrder = ordDirOrderFirstService.selectOne(query);
        if (firstOrder == null) {
            return Response.error("找不到此铺货单");
        }
        String bizOrgCode = UserUtil.getBizOrgCode();
        String orgCode = UserUtil.getOrgCode();
        firstOrder.setBizOrgCode(bizOrgCode);
        firstOrder.setOrgCode(orgCode);
        int count = ordDirOrderFirstService.invalidFirstOrder(firstOrder);
        if (count > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

//    @ApiOperation(value = "审核首单铺货订单", notes = "", httpMethod = "POST")
//    @PostMapping("/audit")
//    public Response<OrdDirOrderFirst> auditDirFirstOrder(@RequestBody InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        // 校验明细数据
//        Response response = ordDirOrderFirstService.checkRepeatParam(insertFirstOrderDetailIn, bizOrgCode);
//        if (!response.isSuccess()) {
//            return response;
//        }
//        OrdDirOrderFirst ordDisOrderFirst = ordDirOrderFirstService.selectByPrimaryKey(insertFirstOrderDetailIn.getFirstOrderId());
//        if (Objects.isNull(ordDisOrderFirst)) {
//            return Response.error("找不到此铺货单");
//        }
//        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
//            return Response.error("只有待审核铺货单可以审核");
//        }
//        return ordDirOrderFirstService.auditFirstOrder(insertFirstOrderDetailIn);
//    }

    @ApiOperation(value = "获取首单铺货订单明细", notes = "", httpMethod = "GET")
    @GetMapping("/findDetailByPage")
    public Response<Page<OrdDirOrderFirstDetailOut>> findOrdDirOrderFirstDetailPage(OrdDirOrderFirstDetailIn ordDisOrderFirstDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        Page<OrdDirOrderFirstDetailOut> firstOrderDetailOutPage = ordDirOrderFirstService.findOrdDirOrderFirstDetailPage(ordDisOrderFirstDetailIn, bizOrgCode);
        return Response.data(firstOrderDetailOutPage);
    }

    @ApiOperation(value = "保存铺货单及明细", notes = "", httpMethod = "POST")
    @PostMapping(value = "/save")
    public Response<OrdDirOrderFirst> saveDirFirstOrderDetail(@RequestBody InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        // 校验明细数据
        Response response = ordDirOrderFirstService.checkRepeatParam(insertFirstOrderDetailIn, bizOrgCode);
        if (!response.isSuccess()) {
            return response;
        }
        // 删除并批量新增数据, 返回单据ID
        return ordDirOrderFirstService.saveFirstOrderDetail(insertFirstOrderDetailIn);
    }

    @PostMapping("/dirFirstToDelivery")
    @ApiOperation(value = "铺货单转直营配货单", notes = "铺货单转直营配货单")
    public void dirFirstToDelivery(@RequestBody JSONObject jsonObject) {
        OrdDirOrderFirst ordDirOrderFirst = JSON.parseObject(jsonObject.toJSONString(), OrdDirOrderFirst.class);
        dirAsyncTaskItemService.dirFirstToDelivery(ordDirOrderFirst);
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
        OrdDirOrderFirst ordDirOrderFirst = ordDirOrderFirstService.getOrdDirOrderFirstForImport(firstOrderId, storeOut, UserUtil.getUserName(), isEffectiveImmediately, effectiveTime);
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以新增明细");
        }
        return ordDirOrderFirstService.asyncImportFirstOrderDetail(fileId, ordDirOrderFirst, UserUtil.getUserName());
    }

    @ApiOperation(value = "审核铺货单", notes = "审核铺货单", httpMethod = "POST")
    @PostMapping("/auditFirstOrder")
    public Response<String> auditFirstOrder(@RequestBody OrdDirFirstOrderAuditIn ordDirFirstOrderAuditIn) {
        String loginUsername = UserUtil.getUserName();
        return ordDirOrderFirstService.auditFirstOrder(ordDirFirstOrderAuditIn, loginUsername);
    }


}
