package com.edc.erp.wholesale.controller;

import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.wholesale.handle.ApiWholesaleHandle;
import com.edc.erp.wholesale.model.in.ApiWholesaleOrderIn;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName ApiWholesaleController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/11 10:10
 **/
@Api(tags = "补货单接口")
@RestController
@RequestMapping("/ord/apiWholesale")
@RequiredArgsConstructor
public class ApiWholesaleController {

    private final ApiWholesaleHandle apiWholesaleHandle;

    @ApiOperation(value = "hs请求ERP批发单据接口", notes = "hs请求ERP批发单据接口", httpMethod = "GET")
    @PostMapping("/requestWholesaleApiTask")
    public Response<String> requestWholesaleApiTask(@RequestBody ApiWholesaleOrderIn apiWholesaleOrderIn) {
        apiWholesaleHandle.handleApiTask(apiWholesaleOrderIn);
        return Response.success("OK");
    }

}
