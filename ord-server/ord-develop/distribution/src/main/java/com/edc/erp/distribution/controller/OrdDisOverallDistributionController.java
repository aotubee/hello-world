package com.edc.erp.distribution.controller;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.model.out.OrdDisOverallDistributionDetailOut;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.distribution.service.OrdDisOverallDistributionService;
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
 * @Description: 统筹配销分货单模块
 * @Author: ZhangYao
 * @Date: 2023/9/18 11:46
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/ord/OrdDisOverallDistribution")
@Api(value = "ordDisOverallDistribution", tags = "统筹配销分货单模块")
public class OrdDisOverallDistributionController {

    private final OrdDisOverallDistributionService ordDisOverallDistributionService;

    private final OrdDisOrderDistributionService ordDisOrderDistributionService;

    @ApiOperation(value = "统筹批量导入配销分货单", notes = "统筹批量导入配销分货单", httpMethod = "GET")
    @GetMapping("/asyncImportOverallDistributionDetail")
    public Response<Long> asyncImportOverallDistributionDetail(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId,
            @ApiParam(name = "分货单id", value = "distributionOrderId") @RequestParam(value = "distributionOrderId", required = false) Long distributionOrderId,
            @ApiParam(name = "是否立即生效", value = "isEffectiveImmediately") @RequestParam(value = "isEffectiveImmediately") Integer isEffectiveImmediately,
            @ApiParam(name = "生效时间", value = "effectiveTime") @RequestParam(value = "effectiveTime", required = false) LocalDateTime effectiveTime,
            @ApiParam(name = "分货标识", value = "distributionIdentification") @RequestParam(value = "distributionIdentification", required = false) String distributionIdentification
    ) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionService.getDisOrderDistributionForImport(distributionOrderId,
                UserUtil.getUserName(), isEffectiveImmediately, effectiveTime, distributionIdentification);
        return ordDisOverallDistributionService.asyncImportOverallDistributionDetail(fileId, ordDisOrderDistribution, UserUtil.getUserName(), distributionIdentification);
    }

    @ApiOperation(value = "根据分货单id获取统筹配销分货单明细", notes = "根据分货单id获取统筹配销分货单明细", httpMethod = "GET")
    @GetMapping("/getOverallDistributionDetailList")
    public Response<OrdDisOverallDistributionDetailOut> getOverallDistributionDetailList(@RequestParam("distributionOrderId") Long distributionOrderId) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionService.getOrdDisOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDisOrderDistribution)) {
            return Response.error("不存在的分货单");
        }
        OrdDisOverallDistributionDetailOut overallDistributionDetailOut = ordDisOverallDistributionService.getOverallDistributionDetailList(ordDisOrderDistribution);
        return Response.data(overallDistributionDetailOut);
    }
}
