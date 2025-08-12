package com.edc.erp.common.controller;

import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.in.datafile.OrdDeliveryDataFileIn;
import com.edc.erp.common.model.out.datafile.OrdDeliveryDataFileOut;
import com.edc.erp.common.service.OrderDeliveryDataFileService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @author tangxiaoliang
 * @description: 配货/配销数据文件上传
 * @since 2023-03-13
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/dataFile")
@Api(value = "配货/配销数据文件上传", tags = "配货/配销数据文件上传")
public class OrderDelivryDataFileController {

    private final OrderDeliveryDataFileService orderDeliveryDataFileService;

    /**
     * 数据文件分页查询
     *
     * @param ordDeliveryDataFileIn
     * @return
     */
    @ApiOperation(value = "数据文件分页查询", notes = "数据文件分页查询")
    @GetMapping("/findByPage")
    public Response<Page<OrdDeliveryDataFileOut>> findByPage(OrdDeliveryDataFileIn ordDeliveryDataFileIn) {
        Page<OrdDeliveryDataFileOut> page = orderDeliveryDataFileService.findByPage(ordDeliveryDataFileIn);
        return Response.data(page);
    }

    @ApiOperation(value = "数据文件新增", notes = "数据文件新增")
    @PostMapping("/save")
    public Response<String> save(@RequestBody OrdDeliveryDataFile ordDeliveryDataFile) {
        ordDeliveryDataFile.setCreator(UserUtil.getUserName());
        orderDeliveryDataFileService.save(ordDeliveryDataFile);
        return Response.success();
    }

    @ApiOperation(value = "下载错误文件", notes = "下载错误文件")
    @GetMapping("/download")
    public Response<String> save(@RequestParam @NotNull Integer id) {
        String url = orderDeliveryDataFileService.download(id);
        if (StringUtils.isBlank(url)) {
            return Response.error("下载失败");
        }
        return Response.data(url, "导出成功");
    }
}
