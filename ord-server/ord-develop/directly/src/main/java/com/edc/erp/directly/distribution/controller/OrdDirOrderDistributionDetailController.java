package com.edc.erp.directly.distribution.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.directly.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.directly.distribution.model.in.SaveDirDistributionDetailIn;
import com.edc.erp.directly.distribution.model.out.DirDistributionCheckOut;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
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
 * 直营分货门店商品关联表 前端控制器
 * </p>
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderDistributionDetail")
@Api(value = "ordDirOrderDistributionDetail", tags = "直营分货门店商品关联表模块")
public class OrdDirOrderDistributionDetailController {

    private final OrdDirOrderDistributionDetailService ordDirOrderDistributionDetailService;

    private final OrdDirOrderDistributionService ordDirOrderDistributionService;

    /**
     * 分页查询直营分货门店商品
     * @param queryOrderDistributionDetailIn 直营分货门店商品 查询入参
     * @return
     */
    @ApiOperation(value = "分页查询直营分货门店商品", notes = "分页查询直营分货门店商品")
    @GetMapping("/findByPage")
    public Response<Page<OrdDirOrderDistributionDetailOut>> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        queryOrderDistributionDetailIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDirOrderDistributionDetailOut> detailOutPage = ordDirOrderDistributionDetailService.findByPage(queryOrderDistributionDetailIn);
        return Response.data(detailOutPage);
    }


    @ApiOperation(value = "删除一个门店分货商品", notes = "删除一个门店分货商品")
    @PostMapping("/delDistributionStoreGoodsByStoreGoodsId")
    public Response delete(@RequestBody DeleteStoreGoodsIn deleteStoreGoodsIn) {
        Response<String> check = ordDirOrderDistributionService.checkDistributionOrderStatus(deleteStoreGoodsIn.getDistributionOrderId());
        if (!check.isSuccess()) {
            return check;
        }
        int deleteCount = ordDirOrderDistributionDetailService.deleteOrdDirOrderDistributionDetail(deleteStoreGoodsIn);
        if (NumberUtil.INTEGER_ZERO.equals(deleteCount)) {
            return Response.error("删除失败");
        }
        return Response.success("删除成功");
    }

    @ApiOperation(value = "修改直营分货门店商品", notes = "修改直营分货门店商品", httpMethod = "POST")
    @PostMapping("/update")
    public Response<String> update(@RequestBody OrdDirOrderDistributionDetail ordDirOrderDistributionDetail) {
        Response<String> check = ordDirOrderDistributionService.checkDistributionOrderStatus(ordDirOrderDistributionDetail.getDistributionOrderId());
        if (!check.isSuccess()) {
            return check;
        }
        int updateCount = ordDirOrderDistributionDetailService.updateOrdDirOrderDistributionDetail(ordDirOrderDistributionDetail);
        if (NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            return Response.error("修改失败");
        }
        return Response.success("修改成功");
    }

    @ApiOperation(value = "查找直营分货门店商品", notes = "", httpMethod = "GET")
    @GetMapping("/findOrdDirOrderDistributions")
    public Response findOrdDirOrderDistributions(@RequestParam Long distributionOrderId, @RequestParam(required = false) String goodsCode, @RequestParam(required = false) String storeCode) {
        return Response.data(ordDirOrderDistributionDetailService.findOrdDirOrderDistributions(distributionOrderId, goodsCode, storeCode));
    }

    @ApiOperation(value = "增加分货明细行", notes = "增加分货明细行", httpMethod = "POST")
    @PostMapping("/createDetailList")
    public Response<Long> createDetailList(@RequestBody SaveDirDistributionDetailIn saveDirDistributionDetailIn) {
        String loginUsername = UserUtil.getUserName();
        if (Objects.isNull(saveDirDistributionDetailIn.getDistributionOrderId())) {
            OrdDirOrderDistribution ordDirOrderDistribution = new OrdDirOrderDistribution();
            ordDirOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDirOrderDistribution.setCreator(loginUsername);
            ordDirOrderDistribution.setUpdater(loginUsername);
            Long dirDistributionOrderId = ordDirOrderDistributionService.createDirDistributionOrder(ordDirOrderDistribution);
            saveDirDistributionDetailIn.setDistributionOrderId(dirDistributionOrderId);
        }
        Response<DirDistributionCheckOut> response = ordDirOrderDistributionDetailService.checkForSaveDetail(saveDirDistributionDetailIn, loginUsername);
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        DirDistributionCheckOut dirDistributionCheckOut = response.getData();
        ordDirOrderDistributionDetailService.saveImportDistributionDetail(dirDistributionCheckOut);
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        if (response.isSuccess() && CollectionUtils.isNotEmpty(dirDistributionCheckOut.getErrorResultList())) {
            Map<String, List<OrdDirDistributionImportErrorResult>> storeErrorMap = dirDistributionCheckOut.getErrorResultList()
                    .stream().collect(Collectors.groupingBy(OrdDirDistributionImportErrorResult::getStoreCode));
            storeErrorMap.entrySet().forEach(entry -> {
                String goodsErrorMsg = entry.getValue().stream().map(errorResult -> errorResult.getGoodsCode() + SystemConstant.SHORT_LINE + errorResult.getErrorMessage()).collect(Collectors.joining(SystemConstant.COMMA));
                errorJoiner.add("门店:" + entry.getKey() + SystemConstant.COMMA + goodsErrorMsg);
            });
        }
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.data(saveDirDistributionDetailIn.getDistributionOrderId(), errorJoiner.toString());
        } else {
            return Response.data(saveDirDistributionDetailIn.getDistributionOrderId());
        }
    }

    @ApiOperation(value = "删除明细", notes = "删除明细", httpMethod = "POST")
    @PostMapping("/deleteDetailById")
    public Response<String> deleteDetailById(@RequestBody OrdDirOrderDistributionDetail ordDirOrderDistributionDetail) {
        OrdDirOrderDistribution orderDistribution = ordDirOrderDistributionService.getOrdDirOrderDistribution(ordDirOrderDistributionDetail.getDistributionOrderId());
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(orderDistribution.getDistributionOrderStatus())) {
            return Response.error("分货单状态不正确");
        }
        ordDirOrderDistributionDetailService.deleteDetailById(ordDirOrderDistributionDetail.getId(), orderDistribution);
        return Response.success("删除明细成功");
    }
}
