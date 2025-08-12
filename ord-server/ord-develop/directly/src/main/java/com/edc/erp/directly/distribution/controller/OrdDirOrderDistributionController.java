package com.edc.erp.directly.distribution.controller;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.handle.DirDistributionOrderAuditHandle;
import com.edc.erp.directly.distribution.model.in.*;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * 直营分货单 前端控制器
 * </p>
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:18
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/ord/ordDirOrderDistribution")
@Api(value = "ordDirOrderDistribution", tags = "直营分货单模块")
public class OrdDirOrderDistributionController {

    private final OrdDirOrderDistributionService ordDirOrderDistributionService;

    private final RedisService redisService;

    private final DirDistributionOrderAuditHandle dirDistributionOrderAuditHandle;


    /**
     * 直营分货单保存或修改
     * @param ordDirOrderDistIn 保存直营分货单和门店商品明细入参类
     * @return
     */
    @ApiOperation(value = "直营分货单保存或修改", notes = "直营分货单保存或修改")
    @PostMapping("/saveUpdateDirOrder")
    public Response<OrdDirOrderDistOut> saveUpdateDirOrder(@RequestBody OrdDirOrderDistIn ordDirOrderDistIn) {
        if (CollectionUtils.isEmpty(ordDirOrderDistIn.getOrdDirOrderDistributionDetails())) {
            return Response.error("至少录入一条门店/商品明细！");
        }

        //业务组织代码
        ordDirOrderDistIn.getOrdDirOrderDistribution().setBizOrgCode(UserUtil.getBizOrgCode());
        //组织代码
        ordDirOrderDistIn.getOrdDirOrderDistribution().setOrgCode(UserUtil.getOrgCode());

        return Response.data(ordDirOrderDistributionService.saveUpdateDirOrder(ordDirOrderDistIn));
    }


    /**
     * 逻辑删除直营分货单
     *
     * @param orderDistribution 直营分货单实体
     * @return
     */
    @ApiOperation(value = "逻辑删除直营分货单", notes = "逻辑删除直营分货订单", httpMethod = "POST")
    @PostMapping("/delDistributionOrder")
    public Response<String> delOrdDirOrderDistribution(@RequestBody OrdDirOrderDistribution orderDistribution) {
        checkOrdDirOrderDistribution(orderDistribution);
        int count = ordDirOrderDistributionService.logicDelOrdDirOrderDistribution(orderDistribution);
        if (NumberUtil.INTEGER_ZERO.equals(count)) {
            return Response.error("删除失败");
        }
        return Response.success("删除成功");
    }


    /**
     * 修改直营分货单
     * @param orderDistribution 直营分货单实体
     * @return
     */
    @ApiOperation(value = "修改直营分货单", notes = "修改直营分货单", httpMethod = "POST")
    @PostMapping("/updatePpDistributionOrder")
    public Response updateOrdDirOrderDistribution(@RequestBody OrdDirOrderDistribution orderDistribution) {
        checkOrdDirOrderDistribution(orderDistribution);
        int count = ordDirOrderDistributionService.updateOrdDirOrderDistribution(orderDistribution);
        if (count > 0) {
            return Response.success("修改成功");
        }
        return Response.success("修改失败 ");
    }

    /**
     * 查询直营分货单
     * @param distributionOrderId 直营分货单id
     * @return
     */
    @ApiOperation(value = "查询直营分货单", notes = "查询直营分货单")
    @GetMapping("/getDisOrderDist")
    public Response<OrdDirOrderDistOut> getDisOrderDist(
            @ApiParam(name = "distributionOrderId", value = "直营分货单id")
            @RequestParam("distributionOrderId") @Valid @NotNull(message = "直营分货单id不能为空") Long distributionOrderId) {

        return ordDirOrderDistributionService.getDisOrderDist(distributionOrderId);
    }


    /**
     * 运营端查询分货单表头
     * @param distributionOrderId 分货单主键
     * @return
     */
    @ApiOperation(value = "运营端查询分货单表头", notes = "运营端查询分货单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderDistributionOrderOutById")
    public Response<BackHeaderOrdDistributionOrderOut> getHeaderOrdDistributionOrderOutById(
            @ApiParam(name = "distributionOrderId", value = "直营分货单id")
            @RequestParam("distributionOrderId") @Valid @NotNull(message = "直营分货单id不能为空") Long distributionOrderId) {
        BackHeaderOrdDistributionOrderOut ordDistributionOrderOut = ordDirOrderDistributionService.getHeaderOrdDistributionOrderOutById(distributionOrderId);
        return Response.data(ordDistributionOrderOut);
    }


    /**
     * 分页查询直营分货单列表
     *
     * @param orderDistributionIn 直营分货单入参
     * @return
     */
    @ApiOperation(value = "分页查询直营分货单列表", notes = "分页查询直营分货单列表", httpMethod = "POST")
    @PostMapping("/findDistributionOrderPage")
    public Response<Page<OrdDirOrderDistributionOrderOut>> findOrdDistributionOrderPage(@RequestBody OrdDirOrderDistributionIn orderDistributionIn) {
        orderDistributionIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDirOrderDistributionOrderOut> distributionPage = ordDirOrderDistributionService.findOrdDistributionOrder(orderDistributionIn);
        return Response.data(distributionPage);
    }


    /**
     * 校验直营分货单是否合法
     * @param orderDistribution 直营分货单入参
     */
    private void checkOrdDirOrderDistribution(OrdDirOrderDistribution orderDistribution) {
        OrdDirOrderDistribution checkDistribution = ordDirOrderDistributionService.getOrdDirOrderDistribution(orderDistribution.getId());
        if (Objects.isNull(checkDistribution)) {
            throw new BusinessException("无效的分货单");
        }
        if (OrderDistributionOrderStatusEnum.EXECUTED.getKey().equals(checkDistribution.getDistributionOrderStatus())) {
            throw new BusinessException("分货单已生效,不可作废");
        }
        if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(checkDistribution.getDistributionOrderStatus())) {
            throw new BusinessException("分货单已作废");
        }
    }


    /**
     * 直营分货单审核
     *
     * @param ordDirOrderDistIn  直营分货单审核入参
     * @return
     */
//    @ApiOperation(value = "直营分货单审核", notes = "直营分货单审核", httpMethod = "POST")
//    @PostMapping("/submitDistributionOrder")
//    public Response<OrdDirOrderDistOut> submitDistributionOrder(@RequestBody OrdDirOrderDistIn ordDirOrderDistIn) {
//        if (Objects.nonNull(ordDirOrderDistIn.getOrdDirOrderDistribution().getId())) {
//            OrdDirOrderDistribution distributionOrder = ordDirOrderDistributionService.getOrdDirOrderDistribution(ordDirOrderDistIn.getOrdDirOrderDistribution().getId());
//            if (Objects.isNull(distributionOrder)) {
//                return Response.error("直营分货单不存在");
//            }
//            if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
//                return Response.error("分货单已作废");
//            }
//            if (OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
//                return Response.error("请勿重复审核");
//            }
//        }
//
//        if (CollectionUtils.isEmpty(ordDirOrderDistIn.getOrdDirOrderDistributionDetails())) {
//            return Response.error("至少录入一条明细");
//        }
//        //添加业务组织和公司code
//        ordDirOrderDistIn.getOrdDirOrderDistribution().setBizOrgCode(UserUtil.getBizOrgCode());
//        ordDirOrderDistIn.getOrdDirOrderDistribution().setOrgCode(UserUtil.getOrgCode());
////        //如果不是立即生效 则为审核状态
////        if(NumberUtil.INTEGER_ZERO.equals(ordDirOrderDistIn.getOrdDirOrderDistribution().getIsEffectiveImmediately())){
//        //审核状态
//        ordDirOrderDistIn.getOrdDirOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
////        }
//        //如果是立即生效 则生成生效时间
//        if (NumberUtil.INTEGER_ONE.equals(ordDirOrderDistIn.getOrdDirOrderDistribution().getIsEffectiveImmediately())) {
////            //生效状态
////            ordDirOrderDistIn.getOrdDirOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
//            //生效时间为当前时间
//            ordDirOrderDistIn.getOrdDirOrderDistribution().setEffectiveTime(LocalDateTime.now());
//
//        }
//        //提交审核
//        OrdDirOrderDistOut out = ordDirOrderDistributionService.audit(ordDirOrderDistIn);
//        if (Objects.isNull(out)) {
//            return Response.error("直营分货单审核失败;");
//        }
//        return Response.data(out);
//    }


    /**
     * 导入门店/商品分货
     * @param importStoreDirOrderIn 导入分货入参
     * @return
     */
    @ApiOperation(value = "导入门店/商品分货", notes = "导入门店/商品分货", httpMethod = "POST")
    @PostMapping("/importDistributionOrder")
    public Response<List<OrdDirOrderDistributionDetailOut>> importOrdDistributionOrder(@RequestBody ImportStoreDirOrderIn importStoreDirOrderIn) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        if (Objects.isNull(importStoreDirOrderIn)) {
            return Response.error("导入参数不能为空");
        }
        if (CollectionUtils.isEmpty(importStoreDirOrderIn.getStoreCodeList())) {
            return Response.error("导入门店不能为空");
        }
        return ordDirOrderDistributionService.importDistributionOrder(importStoreDirOrderIn, userName, bizOrgCode);
    }


    /**
     * 直营分货单作废
     * @param distributionOrderId 直营分货单id
     * @return
     */
    @ApiOperation(value = "直营分货单作废", notes = "直营分货单作废")
    @GetMapping("/invalidDistributionOrder")
    public Response<String> invalidDistributionOrder(@RequestParam @NotNull(message = "直营分货单id不能为空") Long distributionOrderId) {
        //校验分货单是否存在
        OrdDirOrderDistribution ordDirOrderDistribution = new OrdDirOrderDistribution();
        ordDirOrderDistribution.setId(distributionOrderId);
        checkOrdDirOrderDistribution(ordDirOrderDistribution);

        ordDirOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDirOrderDistribution.setUpdater(UserUtil.getNickname());
        ordDirOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.INVALID.getKey());
        ordDirOrderDistributionService.invalidDistributionOrder(ordDirOrderDistribution);
        return Response.success("作废成功");
    }


    /**
     * 直营分货单导出
     * @param queryOrderDistributionDetailIn 直营出货单详情入参
     * @return
     */
    @ApiOperation(value = "直营分货单导出", notes = "直营分货单导出")
    @GetMapping("/exportDirOrderStoreGoodsInfo")
    public Response<String> export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        if (Objects.isNull(queryOrderDistributionDetailIn.getDistributionOrderId())) {
            return Response.error("明细为空");
        }
        //添加业务组织
        queryOrderDistributionDetailIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDirOrderDistributionService.export(queryOrderDistributionDetailIn));
    }


    /**
     * 批量导入分货单
     * @param fileId 文件id
     * @return
     */
    @ApiOperation(value = "批量导入直营分货单", notes = "批量导入直营分货单", httpMethod = "GET")
    @GetMapping("/importDirOrderGoods")
    public Response<List<OrdDirOrderDistributionDetailOut>> importDistributionOrderGoods(
            @ApiParam(name = "文件id", value = "fileId")
            @RequestParam("fileId") String fileId) {

        return ordDirOrderDistributionService.initDirOrderStoreGoodsListener(fileId);
    }


    /**
     * 查询直营分货订单列表
     * @param dirJoinOrderIn 作废直营分货单关联的订货单列表查询入参类
     * @return
     */
    @ApiOperation(value = "查询直营分货订单列表", notes = "查询直营分货订单列表")
    @PostMapping("/findDisJoinOrderListByDisId")
    public Response<Page<DirJoinOrderOut>> findDirJoinOrderListByDisId(@RequestBody DirJoinOrderIn dirJoinOrderIn) {
        dirJoinOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<DirJoinOrderOut> outPage = ordDirOrderDistributionService.findDirJoinOrderListByDisId(dirJoinOrderIn);
        return Response.data(outPage);
    }


    /**
     * 查找直营分货单操作人集合
     * @return
     */
    @ApiOperation(value = "查找直营分货单操作人集合", notes = "查找直营分货单操作人集合", httpMethod = "GET")
    @GetMapping("/findDirOrderCreatorList")
    public Response<List<UserNameOut>> findDirOrderCreatorList() {
        //业务组织
        String bizOrgCode = UserUtil.getBizOrgCode();
        return Response.data(ordDirOrderDistributionService.findDirOrderCreatorList(bizOrgCode));
    }


    @ApiOperation(value = "创建分货单（新）", notes = "创建分货单（新）", httpMethod = "POST")
    @PostMapping("/createDirDistributionOrder")
    public Response<OrdDirOrderDistribution> createDirDistributionOrder() {
        OrdDirOrderDistribution ordDirOrderDistribution = new OrdDirOrderDistribution();
        ordDirOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
        ordDirOrderDistribution.setCreator(UserUtil.getUserName());
        ordDirOrderDistribution.setUpdater(UserUtil.getUserName());
        ordDirOrderDistributionService.createDirDistributionOrder(ordDirOrderDistribution);
        return Response.data(ordDirOrderDistribution, "创建成功");
    }

    @ApiOperation(value = "批量导入直营分货单", notes = "批量导入直营分货单", httpMethod = "GET")
    @GetMapping("/asyncImportDistributionDetail")
    public Response<Long> asyncImportDistributionDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId,
            @ApiParam(name = "分货单id", value = "distributionOrderId") @RequestParam(value = "distributionOrderId", required = false) Long distributionOrderId,
            @ApiParam(name = "是否立即生效", value = "isEffectiveImmediately") @RequestParam(value = "isEffectiveImmediately") Integer isEffectiveImmediately,
            @ApiParam(name = "生效时间", value = "effectiveTime") @RequestParam(value = "effectiveTime", required = false) LocalDateTime effectiveTime) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionService.getDirOrderDistributionForImport(distributionOrderId, UserUtil.getUserName(), isEffectiveImmediately, effectiveTime);
        return ordDirOrderDistributionService.asyncImportDistributionDetail(fileId, ordDirOrderDistribution, UserUtil.getUserName());
    }

    @ApiOperation(value = "审核分货单", notes = "审核分货单", httpMethod = "POST")
    @PostMapping("/auditDirDistribution")
    public Response<String> auditDirDistribution(@RequestBody OrdDirDistributionAuditIn ordDirDistributionAuditIn) {
        String loginUsername = UserUtil.getUserName();
        return ordDirOrderDistributionService.handleAudit(ordDirDistributionAuditIn, loginUsername);
    }

    @ApiOperation(value = "修改分货单生效时机", notes = "修改分货单生效时机", httpMethod = "POST")
    @PostMapping("/updateHead")
    public Response<String> updateHead(@RequestBody UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn) {
        return ordDirOrderDistributionService.updateHead(updateDistributionEffectiveTimeIn);
    }
}
