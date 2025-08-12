package com.edc.erp.distribution.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.distribution.model.in.SaveDisDistributionDetailIn;
import com.edc.erp.distribution.model.out.DisDistributionCheckOut;
import com.edc.erp.distribution.model.out.OrdDisOrderDistributionDetailOut;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * <p>
 * 配销分货门店商品关联表 前端控制器
 * </p>
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderDistributionDetail")
@Api(value = "ordDisOrderDistributionDetail", tags = "配销分货门店商品关联表模块")
public class OrdDisOrderDistributionDetailController {

    private final OrdDisOrderDistributionDetailService ordDisOrderDistributionDetailService;

    private final OrdDisOrderDistributionService ordDisOrderDistributionService;

    /**
     * 分页查询配销分货门店商品
     * @param queryOrderDistributionDetailIn 配销分货门店商品 查询入参
     * @return
     */
    @ApiOperation(value = "分页查询配销分货门店商品", notes = "分页查询配销分货门店商品")
    @GetMapping("/findByPage")
    public Response<Page<OrdDisOrderDistributionDetailOut>> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        queryOrderDistributionDetailIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDisOrderDistributionDetailOut> detailOutPage = ordDisOrderDistributionDetailService.findByPage(queryOrderDistributionDetailIn);
        return Response.data(detailOutPage);
    }


    @ApiOperation(value = "删除一个门店分货商品", notes = "删除一个门店分货商品")
    @PostMapping("/delDistributionStoreGoodsByStoreGoodsId")
    public Response delete(@RequestBody DeleteStoreGoodsIn deleteStoreGoodsIn) {
        Response<String> check = ordDisOrderDistributionService.checkDistributionOrderStatus(deleteStoreGoodsIn.getDistributionOrderId());
        if (!check.isSuccess()) {
            return check;
        }
        int deleteCount = ordDisOrderDistributionDetailService.deleteOrdDisOrderDistributionDetail(deleteStoreGoodsIn);
        if (NumberUtil.INTEGER_ZERO.equals(deleteCount)) {
            return Response.error("删除失败");
        }
        return Response.success("删除成功");
    }

    @ApiOperation(value = "修改配销分货门店商品", notes = "修改配销分货门店商品", httpMethod = "POST")
    @PostMapping("/update")
    public Response<String> update(@RequestBody OrdDisOrderDistributionDetail ordDisOrderDistributionDetail) {
        Response<String> check = ordDisOrderDistributionService.checkDistributionOrderStatus(ordDisOrderDistributionDetail.getDistributionOrderId());
        if (!check.isSuccess()) {
            return check;
        }
        int updateCount = ordDisOrderDistributionDetailService.updateOrdDisOrderDistributionDetail(ordDisOrderDistributionDetail);
        if (NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            return Response.error("修改失败");
        }
        return Response.success("修改成功");
    }

    @ApiOperation(value = "导出配销分货门店商品", notes = "", httpMethod = "GET")
    @GetMapping("/export")
    public Response export(@RequestParam Long distributionOrderId, @RequestParam(required = false) String goodsCode, @RequestParam(required = false) String storeCode) {
        ordDisOrderDistributionDetailService.export(distributionOrderId, goodsCode, storeCode);
        return Response.success("导出成功");
    }

    @ApiOperation(value = "查找配销分货门店商品", notes = "", httpMethod = "GET")
    @GetMapping("/findOrdDisOrderDistributions")
    public Response findOrdDisOrderDistributions(@RequestParam Long distributionOrderId, @RequestParam(required = false) String goodsCode, @RequestParam(required = false) String storeCode) {
        return Response.data(ordDisOrderDistributionDetailService.findOrdDisOrderDistributions(distributionOrderId, goodsCode, storeCode));
    }

    @ApiOperation(value = "增加分货明细行", notes = "增加分货明细行", httpMethod = "POST")
    @PostMapping("/createDetailList")
    public Response<Long> createDetailList(@RequestBody SaveDisDistributionDetailIn saveDisDistributionDetailIn) {
        String loginUsername = UserUtil.getUserName();
        if (Objects.isNull(saveDisDistributionDetailIn.getDistributionOrderId())) {
            OrdDisOrderDistribution ordDisOrderDistribution = new OrdDisOrderDistribution();
            ordDisOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDisOrderDistribution.setCreator(loginUsername);
            ordDisOrderDistribution.setUpdater(loginUsername);
            ordDisOrderDistribution.setDistributionIdentification(saveDisDistributionDetailIn.getDistributionIdentification());
            Long disDistributionOrderId = ordDisOrderDistributionService.createDisDistributionOrder(ordDisOrderDistribution);
            saveDisDistributionDetailIn.setDistributionOrderId(disDistributionOrderId);
        }
        Response<DisDistributionCheckOut> response = ordDisOrderDistributionDetailService.checkForSaveDetail(saveDisDistributionDetailIn,
                loginUsername, saveDisDistributionDetailIn.getDistributionIdentification());
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        DisDistributionCheckOut disDistributionCheckOut = response.getData();
        ordDisOrderDistributionDetailService.saveImportDistributionDetail(disDistributionCheckOut);
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        if (response.isSuccess() && CollectionUtils.isNotEmpty(disDistributionCheckOut.getErrorResultList())) {
            Map<String, List<OrdDisDistributionImportErrorResult>> storeErrorMap = disDistributionCheckOut.getErrorResultList()
                    .stream().collect(Collectors.groupingBy(OrdDisDistributionImportErrorResult::getStoreCode));
            storeErrorMap.entrySet().forEach(entry -> {
                String goodsErrorMsg = entry.getValue().stream().map(errorResult -> errorResult.getGoodsCode() + SystemConstant.SHORT_LINE + errorResult.getErrorMessage()).collect(Collectors.joining(SystemConstant.COMMA));
                errorJoiner.add("门店:" + entry.getKey() + SystemConstant.COMMA + goodsErrorMsg);
            });
        }
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.data(saveDisDistributionDetailIn.getDistributionOrderId(), errorJoiner.toString());
        } else {
            return Response.data(saveDisDistributionDetailIn.getDistributionOrderId());
        }
    }

    @ApiOperation(value = "删除明细", notes = "删除明细", httpMethod = "POST")
    @PostMapping("/deleteDetailById")
    public Response<String> deleteDetailById(@RequestBody OrdDisOrderDistributionDetail ordDisOrderDistributionDetail) {
        OrdDisOrderDistribution orderDistribution = ordDisOrderDistributionService.getOrdDisOrderDistribution(ordDisOrderDistributionDetail.getDistributionOrderId());
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(orderDistribution.getDistributionOrderStatus())) {
            return Response.error("分货单状态不正确");
        }
        ordDisOrderDistributionDetailService.deleteDetailById(ordDisOrderDistributionDetail.getId(), orderDistribution);
        return Response.success("删除明细成功");
    }
}
