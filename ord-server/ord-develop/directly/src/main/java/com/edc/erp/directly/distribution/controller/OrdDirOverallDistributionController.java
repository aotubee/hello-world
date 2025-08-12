package com.edc.erp.directly.distribution.controller;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.model.out.OrdDirOverallDistributionDetailOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.distribution.service.OrdDirOverallDistributionService;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * @Description: 统筹直营分货单模块
 * @Author: ZhangYao
 * @Date: 2023/9/18 11:46
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/ord/OrdDirOverallDistribution")
@Api(value = "ordDirOverallDistribution", tags = "统筹直营分货单模块")
public class OrdDirOverallDistributionController {

    private final OrdDirOverallDistributionService ordDirOverallDistributionService;

    private final OrdDirOrderDistributionService ordDirOrderDistributionService;

    @ApiOperation(value = "统筹批量导入直营分货单", notes = "统筹批量导入直营分货单", httpMethod = "GET")
    @GetMapping("/asyncImportOverallDistributionDetail")
    public Response<Long> asyncImportOverallDistributionDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId,
            @ApiParam(name = "分货单id", value = "distributionOrderId") @RequestParam(value = "distributionOrderId", required = false) Long distributionOrderId,
            @ApiParam(name = "是否立即生效", value = "isEffectiveImmediately") @RequestParam(value = "isEffectiveImmediately") Integer isEffectiveImmediately,
            @ApiParam(name = "生效时间", value = "effectiveTime") @RequestParam(value = "effectiveTime", required = false) LocalDateTime effectiveTime) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionService.getDirOrderDistributionForImport(distributionOrderId, UserUtil.getUserName(), isEffectiveImmediately, effectiveTime);
        return ordDirOverallDistributionService.asyncImportOverallDistributionDetail(fileId, ordDirOrderDistribution, UserUtil.getUserName());
    }

    @ApiOperation(value = "根据分货单id获取统筹直营分货单明细", notes = "根据分货单id获取统筹直营分货单明细", httpMethod = "GET")
    @GetMapping("/getOverallDistributionDetailList")
    public Response<OrdDirOverallDistributionDetailOut> getOverallDistributionDetailList(@RequestParam("distributionOrderId") Long distributionOrderId) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionService.getOrdDirOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDirOrderDistribution)) {
            return Response.error("不存在的分货单");
        }
        OrdDirOverallDistributionDetailOut overallDistributionDetailOut = ordDirOverallDistributionService.getOverallDistributionDetailList(ordDirOrderDistribution);
        return Response.data(overallDistributionDetailOut);
    }
}
