/**
 * Copyright © 2010-2023 Everyday Chain. All rights reserved.
 */
package com.edc.erp.directly.upperlowerlimit.controller;

import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.upperlowerlimit.job.DirUpperLowerLimitOrderScheduler;
import com.edc.erp.directly.upperlowerlimit.server.DirUpperLowerLimitServer;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 补货单接口
 *
 * @author: gusiyuan
 * @date: 2023-01-11
 */
@Api(tags = "补货单接口")
@RestController
@RequestMapping("/ord/dirUpperLowerLimit")
public class DirUpperLowerLimitController {

    @Autowired
    private DirUpperLowerLimitServer dirUpperLowerLimitServer;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DirUpperLowerLimitOrderScheduler dirUpperLowerLimitOrderScheduler;

    @ApiOperation(value = "按门店代码批量跑补货单", notes = "", httpMethod = "GET")
    @GetMapping("/replenishmentOrderJobByStoreList")
    public Response replenishmentOrderJobByStoreList(@RequestParam(required = false) String storeCodeStr) {
        List<String> storeCodeList;
        if (StringUtils.isEmpty(storeCodeStr)) {
            storeCodeList = storeCenterService.findIsAutoReplenishmentStoreCodeList(UserUtil.getBizOrgCode(), StoreConstant.StoreProperty.DIRECTLY.getMytValue());
        } else {
            storeCodeList = Arrays.asList(storeCodeStr.split(","));
        }
        dirUpperLowerLimitServer.replenishmentOrderJob(storeCodeList, UserUtil.getBizOrgCode());
        return Response.success("OK");
    }

//    @ApiOperation(value = "dss手动全量Job跑补货单", notes = "", httpMethod = "GET")
//    @GetMapping("/dssDirJob")
//    public Response dssDirJob() {
//        dirUpperLowerLimitOrderScheduler.dssReplenishmentOrderDirectlyJob();
//        return Response.success();
//    }
}
