package com.edc.erp.distribution.controller;

import com.edc.erp.distribution.model.in.DisOrderTrackIn;
import com.edc.erp.distribution.model.out.DisOrderTrackElementOut;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;


/**
 * <p>
 * 配销订单追踪表 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderTrack")
@Api(value = "ordDisOrderTrack", tags = "配销订单追踪表模块")
public class OrdDisOrderTrackController {

    private final OrdDisOrderTrackService ordDisOrderTrackService;

    /**
     * 根据单号查询业务日志
     */
    @ApiOperation(value = "根据单号查询业务日志", notes = "根据单号查询业务日志")
    @PostMapping("/findOrderTrackList")
    public Response<List<DisOrderTrackElementOut>> findOrderTrackList(@RequestBody @Valid DisOrderTrackIn disOrderTrackIn){
        String bizOrgCode = UserUtil.getBizOrgCode();
        disOrderTrackIn.setBizOrgCode(bizOrgCode);
        return Response.data(ordDisOrderTrackService.findOrderTrackOutListByParameters(disOrderTrackIn));
    }
}
