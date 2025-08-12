package com.edc.erp.distribution.controller;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
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
 * 配销分货单 前端控制器
 * </p>
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:18
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/ord/ordDisOrderDistribution")
@Api(value = "ordDisOrderDistribution", tags = "配销分货单模块")
public class OrdDisOrderDistributionController {

    private final OrdDisOrderDistributionService ordDisOrderDistributionService;

    /**
     * 配销分货单保存或修改
     * @param ordDisOrderDistIn 保存配销分货单和门店商品明细入参
     * @return
     */
    @ApiOperation(value = "配销分货单保存或修改", notes = "配销分货单保存或修改")
    @PostMapping("/saveUpdateDisOrder")
    public Response<OrdDisOrderDistOut> saveUpdateDisOrder(@RequestBody OrdDisOrderDistIn ordDisOrderDistIn) {
        if (CollectionUtils.isEmpty(ordDisOrderDistIn.getOrdDisOrderDistributionDetails())) {
            return Response.error("至少录入一条门店商品明细！");
        }

        //业务组织代码
        ordDisOrderDistIn.getOrdDisOrderDistribution().setBizOrgCode(UserUtil.getBizOrgCode());
        //组织代码
        ordDisOrderDistIn.getOrdDisOrderDistribution().setOrgCode(UserUtil.getOrgCode());
        return Response.data(ordDisOrderDistributionService.saveUpdateDisOrder(ordDisOrderDistIn));
    }


    /**
     * 逻辑删除配销分货单
     *
     * @param orderDistribution
     * @return
     */
    @ApiOperation(value = "逻辑删除配销分货单", notes = "逻辑删除分货订单", httpMethod = "POST")
    @PostMapping("/delDistributionOrder")
    public Response<String> delOrdDisOrderDistribution(@RequestBody OrdDisOrderDistribution orderDistribution) {
        checkOrdDisOrderDistribution(orderDistribution);
        int count = ordDisOrderDistributionService.logicDelOrdDisOrderDistribution(orderDistribution);
        if (NumberUtil.INTEGER_ZERO.equals(count)) {
            return Response.error("删除失败");
        }
        return Response.success("删除成功");
    }


    /**
     * 修改配销分货单
     *
     * @param orderDistribution
     * @return
     */
    @ApiOperation(value = "修改配销分货单", notes = "修改配销分货单", httpMethod = "POST")
    @PostMapping("/updatePpDistributionOrder")
    public Response<String> updateOrdDisOrderDistribution(@RequestBody OrdDisOrderDistribution orderDistribution) {
        checkOrdDisOrderDistribution(orderDistribution);
        int count = ordDisOrderDistributionService.updateOrdDisOrderDistribution(orderDistribution);
        if (count > 0) {
            return Response.success("修改成功");
        }
        return Response.success("修改失败 ");
    }


    /**
     * 查询配销分货单
     * @param distributionOrderId 分货单id
     * @return
     */
    @ApiOperation(value = "查询配销分货单", notes = "查询配销分货单")
    @GetMapping("/getDisOrderDist")
    public Response<OrdDisOrderDistOut> getDisOrderDist(
            @ApiParam(name = "distributionOrderId", value = "配销分货单id")
            @RequestParam(value = "distributionOrderId") @Valid @NotNull(message = "配销分货单id不能为空") Long distributionOrderId) {

        return ordDisOrderDistributionService.getDisOrderDist(distributionOrderId);
    }


    /**
     * 运营端查询分货单表头
     *
     * @param distributionOrderId 分货单主键
     * @return
     */
    @ApiOperation(value = "运营端查询分货单表头", notes = "运营端查询分货单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderDistributionOrderOutById")
    public Response<BackHeaderOrdDistributionOrderOut> getHeaderOrdDistributionOrderOutById(
            @ApiParam(name = "distributionOrderId", value = "配销分货单id")
            @RequestParam("distributionOrderId") @Valid @NotNull(message = "配销分货单id不能为空") Long distributionOrderId) {

        BackHeaderOrdDistributionOrderOut ordDistributionOrderOut = ordDisOrderDistributionService.getHeaderOrdDistributionOrderOutById(distributionOrderId);
        return Response.data(ordDistributionOrderOut);
    }


    /**
     * 分页查询配销分货单列表
     *
     * @param orderDistributionIn
     * @return
     */
    @ApiOperation(value = "分页查询配销分货单列表", notes = "分页查询配销分货单列表", httpMethod = "POST")
    @PostMapping("/findDistributionOrderPage")
    public Response<Page<OrdDisOrderDistributionOrderOut>> findOrdDistributionOrderByPage(@RequestBody OrdDisOrderDistributionIn orderDistributionIn) {
        orderDistributionIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDisOrderDistributionOrderOut> distributionPage = ordDisOrderDistributionService.findOrdDistributionOrder(orderDistributionIn);
        return Response.data(distributionPage);
    }


    /**
     * 校验配销分货单是否合法
     *
     * @param orderDistribution 配销分货单实体
     */
    private void checkOrdDisOrderDistribution(OrdDisOrderDistribution orderDistribution) {
        OrdDisOrderDistribution checkDistribution = ordDisOrderDistributionService.getOrdDisOrderDistribution(orderDistribution.getId());
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
     * 配销分货单作废
     *
     * @param ordDistributionOrderId 配销分货单实体
     * @return
     */
    @ApiOperation(value = "配销分货单作废", notes = "配销分货单作废", httpMethod = "GET")
    @GetMapping("/invalidDistributionOrder")
    public Response<String> invalidDistributionOrder(@RequestParam @Valid @NotNull(message = "配销分货单id不能为空") Long ordDistributionOrderId) {
        //校验分货单是否存在
        OrdDisOrderDistribution ordDisOrderDistribution = new OrdDisOrderDistribution();
        ordDisOrderDistribution.setId(ordDistributionOrderId);
        checkOrdDisOrderDistribution(ordDisOrderDistribution);

        String loginUsername = UserUtil.getUserName();
        ordDisOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistribution.setUpdater(loginUsername);
        ordDisOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.INVALID.getKey());
        ordDisOrderDistributionService.invalidDistributionOrder(ordDisOrderDistribution);
        return Response.success("作废成功");
    }


    /**
     * 配销分货单审核
     *
     * @param ordDisOrderDistIn 配销分货单审核入参
     * @return
     */
//    @ApiOperation(value = "配销分货单审核", notes = "配销分货单审核", httpMethod = "POST")
//    @PostMapping("/submitDistributionOrder")
//    public Response<OrdDisOrderDistOut> audit(@RequestBody OrdDisOrderDistIn ordDisOrderDistIn) {
//        if (Objects.nonNull(ordDisOrderDistIn.getOrdDisOrderDistribution().getId())) {
//            OrdDisOrderDistribution distributionOrder = ordDisOrderDistributionService.getOrdDisOrderDistribution(ordDisOrderDistIn.getOrdDisOrderDistribution().getId());
//            if (Objects.isNull(distributionOrder)) {
//                return Response.error("配销分货单不存在");
//            }
//            if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
//                return Response.error("分货单已作废");
//            }
//            if (OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
//                return Response.error("请勿重复审核");
//            }
//        }
//
//        if (CollectionUtils.isEmpty(ordDisOrderDistIn.getOrdDisOrderDistributionDetails())) {
//            return Response.error("至少录入一条明细");
//        }
//        //添加业务组织和公司code
//        ordDisOrderDistIn.getOrdDisOrderDistribution().setBizOrgCode(UserUtil.getBizOrgCode());
//        ordDisOrderDistIn.getOrdDisOrderDistribution().setOrgCode(UserUtil.getOrgCode());
//        //如果不是立即生效 则为审核状态
////        if(NumberUtil.INTEGER_ZERO.equals(ordDisOrderDistIn.getOrdDisOrderDistribution().getIsEffectiveImmediately())){
//        //审核状态
//        ordDisOrderDistIn.getOrdDisOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
////        }
//        //如果是立即生效 则生成生效时间
//        if (NumberUtil.INTEGER_ONE.equals(ordDisOrderDistIn.getOrdDisOrderDistribution().getIsEffectiveImmediately())) {
////            //生效状态
////            ordDisOrderDistIn.getOrdDisOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
//            //立即生效时间为当前时间
//            ordDisOrderDistIn.getOrdDisOrderDistribution().setEffectiveTime(LocalDateTime.now());
//        }
//        //提交审核
//        OrdDisOrderDistOut out = ordDisOrderDistributionService.audit(ordDisOrderDistIn);
//        if (Objects.isNull(out)) {
//            return Response.error("配销分货单审核失败;");
//        }
//        return Response.data(out);
//    }


    /**
     * 批量导入分货单
     * @param fileId 文件id
     * @return
     */
    @ApiOperation(value = "批量导入分货单", notes = "批量导入分货单", httpMethod = "GET")
    @GetMapping("/importDistributionOrderGoods")
    public Response<List<OrdDisOrderDistributionDetailOut>> importDistributionOrderGoods(
            @ApiParam(name = "文件id", value = "fileId")
            @RequestParam("fileId") String fileId) {

        return ordDisOrderDistributionService.initDistributionOrderStoreGoodsListener(fileId);
    }


    /**
     * 导入门店/商品分货
     * @param importStoreDistributionOrderIn 导入分货入参
     * @return
     */
    @ApiOperation(value = "导入门店/商品分货", notes = "导入门店/商品分货", httpMethod = "POST")
    @PostMapping("/importDistributionOrder")
    public Response<List<OrdDisOrderDistributionDetailOut>> importOrdDistributionOrder(@RequestBody ImportStoreDistributionOrderIn importStoreDistributionOrderIn) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        if (Objects.isNull(importStoreDistributionOrderIn)) {
            return Response.error("导入参数不能为空");
        }
        if (CollectionUtils.isEmpty(importStoreDistributionOrderIn.getStoreCodeList())) {
            return Response.error("导入门店不能为空");
        }
        return ordDisOrderDistributionService.importDistributionOrder(importStoreDistributionOrderIn, userName, bizOrgCode);
    }


    /**
     * 查询分货订单列表
     * @param disJoinOrderIn 作废分货单关联的订货单列表查询入参类
     * @return
     */
    @ApiOperation(value = "查询分货订单列表", notes = "查询分货订单列表")
    @PostMapping("/findDisJoinOrderListByDisId")
    public Response<Page<DisJoinOrderOut>> findDisJoinOrderListByDisId(@RequestBody DisJoinOrderIn disJoinOrderIn) {
        disJoinOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<DisJoinOrderOut> outPage = ordDisOrderDistributionService.findDisJoinOrderListByDisId(disJoinOrderIn);
        return Response.data(outPage);
    }


    /**
     * 配销分货单导出
     * @param queryOrderDistributionDetailIn 出货单详情入参
     * @return
     */
    @ApiOperation(value = "配销分货单导出", notes = "配销分货单导出")
    @GetMapping("/exportDistributionStoreGoodsInfo")
    public Response<String> export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        if (Objects.isNull(queryOrderDistributionDetailIn.getDistributionOrderId())) {
            return Response.error("明细为空");
        }
        //添加业务组织
        queryOrderDistributionDetailIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDisOrderDistributionService.export(queryOrderDistributionDetailIn));
    }


    /**
     * 查找配销分货单操作人集合
     * @return
     */
    @ApiOperation(value = "查找配销分货单操作人集合", notes = "查找配销分货单操作人集合", httpMethod = "GET")
    @GetMapping("/findDistributionOrderCreatorList")
    public Response<List<UserNameOut>> findDistributionOrderCreatorList() {
        //业务组织
        String bizOrgCode = UserUtil.getBizOrgCode();
        return Response.data(ordDisOrderDistributionService.findDistributionOrderCreatorList(bizOrgCode));
    }


    @ApiOperation(value = "创建分货单（新）", notes = "创建分货单（新）", httpMethod = "POST")
    @PostMapping("/createDirDistributionOrder")
    public Response<OrdDisOrderDistribution> createDirDistributionOrder() {
        OrdDisOrderDistribution ordDisOrderDistribution = new OrdDisOrderDistribution();
        ordDisOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
        ordDisOrderDistribution.setCreator(UserUtil.getUserName());
        ordDisOrderDistribution.setUpdater(UserUtil.getUserName());
        ordDisOrderDistributionService.createDisDistributionOrder(ordDisOrderDistribution);
        return Response.data(ordDisOrderDistribution, "创建成功");
    }

    @ApiOperation(value = "批量导入直营分货单", notes = "批量导入直营分货单", httpMethod = "GET")
    @GetMapping("/asyncImportDistributionDetail")
    public Response<Long> asyncImportDistributionDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId,
            @ApiParam(name = "分货单id", value = "distributionOrderId") @RequestParam(value = "distributionOrderId", required = false) Long distributionOrderId,
            @ApiParam(name = "是否立即生效", value = "isEffectiveImmediately") @RequestParam(value = "isEffectiveImmediately") Integer isEffectiveImmediately,
            @ApiParam(name = "生效时间", value = "effectiveTime") @RequestParam(value = "effectiveTime", required = false) LocalDateTime effectiveTime,
            @ApiParam(name = "分货标识", value = "distributionIdentification") @RequestParam(value = "distributionIdentification", required = false) String distributionIdentification
    ) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionService.getDisOrderDistributionForImport(distributionOrderId,
                UserUtil.getUserName(), isEffectiveImmediately, effectiveTime, distributionIdentification);
        return ordDisOrderDistributionService.asyncImportDistributionDetail(fileId, ordDisOrderDistribution, UserUtil.getUserName(), distributionIdentification);
    }

    @ApiOperation(value = "审核分货单", notes = "审核分货单", httpMethod = "POST")
    @PostMapping("/auditDisDistribution")
    public Response<String> auditDisDistribution(@RequestBody OrdDisDistributionAuditIn ordDirDistributionAuditIn) {
        String loginUsername = UserUtil.getUserName();
        return ordDisOrderDistributionService.handleAudit(ordDirDistributionAuditIn, loginUsername);
    }

    @ApiOperation(value = "修改分货单生效时机", notes = "修改分货单生效时机", httpMethod = "POST")
    @PostMapping("/updateHead")
    public Response<String> updateHead(@RequestBody UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn) {
        return ordDisOrderDistributionService.updateHead(updateDistributionEffectiveTimeIn);
    }
}
