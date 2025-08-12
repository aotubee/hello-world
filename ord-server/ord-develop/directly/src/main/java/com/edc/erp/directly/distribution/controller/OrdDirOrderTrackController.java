package com.edc.erp.directly.distribution.controller;

import com.edc.erp.directly.distribution.model.in.DirOrderTrackIn;
import com.edc.erp.directly.distribution.model.out.DirOrderTrackElementOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
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
 * 直营订单追踪表 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-11-18 18:46:57
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderTrack")
@Api(value = "ordDirOrderTrack", tags = "直营订单追踪表模块")
public class OrdDirOrderTrackController {

    private final OrdDirOrderTrackService ordDirOrderTrackService;

    /**
     * 根据单号查询业务日志
     */
    @ApiOperation(value = "根据单号查询业务日志", notes = "根据单号查询业务日志")
    @PostMapping("/findOrderTrackList")
    public Response<List<DirOrderTrackElementOut>> findOrderTrackList(@RequestBody @Valid DirOrderTrackIn dirOrderTrackIn){
        String bizOrgCode = UserUtil.getBizOrgCode();
        dirOrderTrackIn.setBizOrgCode(bizOrgCode);
        return Response.data(ordDirOrderTrackService.findOrderTrackOutListByParameters(dirOrderTrackIn));
    }
}
