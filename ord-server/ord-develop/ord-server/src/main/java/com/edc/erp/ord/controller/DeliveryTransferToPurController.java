package com.edc.erp.ord.controller;

import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.model.out.fl.DeliveryNumberVO;
import com.edc.erp.ord.job.TransferDeliveryOrderScheduler;
import com.edc.plugins.common.response.Response;
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

/**
 * Description
 *
 * @author : lee
 * @date 2023-02-04
 */
@Api(tags = "中转配送控制器")
@RestController
@RequestMapping("/ord/deliveryTrfToPur")
@Slf4j
public class DeliveryTransferToPurController {

    @Autowired
    private TransferDeliveryOrderScheduler transferDirDeliveryOrderScheduler;

//    @ApiOperation(value = "中转下发采购定时任务", notes = "中转下发采购定时任务", httpMethod = "GET")
//    @GetMapping("/autoTransferDeliveryOrderJob")
//    public Response<DeliveryNumberVO> autoTransferDeliveryOrderJob(@RequestParam(required = false) String bizOrgCode) {
//        if (StringUtils.isBlank(bizOrgCode)) {
//            bizOrgCode = UserUtil.getBizOrgCode();
//        }
//        if (OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode().equals(bizOrgCode)) {
//            transferDirDeliveryOrderScheduler.autoXIANTransferDeliveryOrderJob();
//        }
//        if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(bizOrgCode)) {
//            transferDirDeliveryOrderScheduler.autoTSTransferDeliveryOrderJob();
//        }
//        if (OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode().equals(bizOrgCode)) {
//            transferDirDeliveryOrderScheduler.autoZHENGZHOUTransferDeliveryOrderJob();
//        }
//        return Response.success();
//    }
}
