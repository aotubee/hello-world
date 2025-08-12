package com.edc.erp.controller;

import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.dictionary.controller.SystemDictController;
import com.edc.sdk.dictionary.entity.SystemDict;
import com.edc.sdk.dictionary.service.SystemDictService;
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

import java.util.List;


/**
 * <p>
 *  系统字典前端控制器
 * </p>
 *
 * @author lx
 * @since 2022-10-18 19:17:55
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/systemDict")
@Api(value = "systemDict", tags = "系统字典接口")
public class OrdSystemDictController extends SystemDictController {

    private final SystemDictService systemDictService;

    /**
     * 系统字典接口
     * @param parentNo
     * @return
     */
    @ApiOperation(value = "系统字典接口", notes = "系统字典接口")
    @GetMapping("/shipmentStatusDictList")
    public Response<List<SystemDict>> findSystemDictListByParentType(
            @ApiParam(name = "parentNo", value = "parentNo")
            @RequestParam(value = "parentNo", required = false) String parentNo) {

        if (StringUtils.isBlank(parentNo)) {
            return Response.error("字典父代码不能为空");
        }

        List<SystemDict> systemDictListByParentNo = systemDictService.findSystemDictList(SystemConstant.SYSTEM_CODE, parentNo);
        return Response.data(systemDictListByParentNo);
    }

    @ApiOperation(value = "系统字典接口", notes = "系统字典接口")
    @GetMapping("/findDisWhy")
    public Response<List<SystemDict>> findDisWhy(
            @ApiParam(name = "parentNo", value = "parentNo")
            @RequestParam(value = "parentNo", required = false) String parentNo) {

        if (StringUtils.isBlank(parentNo)) {
            return Response.error("字典父代码不能为空");
        }

        List<SystemDict> systemDictListByParentNo = systemDictService.findSystemDictList("inv", parentNo);
        return Response.data(systemDictListByParentNo);
    }

    /**
     * 多个父代码查询字典项
     * @param parents 多个父代码拼接
     * @return
     */
    @Override
    @ApiOperation(value = "多个父代码查询字典项", notes = "多个父代码查询字典项")
    @GetMapping("/findParentsList")
    public Response findParentsList(String parents) {
        return super.findParentsList(parents);
    }
}
