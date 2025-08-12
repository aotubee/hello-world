package com.edc.erp.presale.controller;

import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.model.in.QueryPresaleAssetsDetailPageIn;
import com.edc.erp.presale.model.in.QueryPresaleAssetsPageIn;
import com.edc.erp.presale.model.out.DisStorePresaleAssetsDetailInfoOut;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsPageOut;
import com.edc.erp.presale.model.out.PresaleAssetsForAppOut;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @ClassName OrdDisPresaleActivityController
 * @Author ZhangYao
 * @CreateTime 2024/8/23 8:43
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disPresaleAssets")
@Api(value = "ordDisPresaleAssetsController", tags = "预售资产接口")
public class OrdDisPresaleAssetsController {
    private final OrdDisPresaleAssetsService ordDisPresaleAssetsService;
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

    /**
     * 分页查询预售资产
     *
     * @param queryPresaleAssetsPageIn
     * @return
     */
    @ApiOperation(value = "分页查询预售资产", notes = "分页查询预售资产", httpMethod = "POST")
    @PostMapping("/findStorePresaleAssetsForPage")
    public Response<Page<OrdDisPresaleAssetsPageOut>> findStorePresaleAssetsForPage(@RequestBody QueryPresaleAssetsPageIn queryPresaleAssetsPageIn) {
        queryPresaleAssetsPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDisPresaleAssetsPageOut> page = ordDisPresaleAssetsService.findStorePresaleAssetsForPage(queryPresaleAssetsPageIn);
        return Response.data(page);
    }

    /**
     * 导出预售资产
     */
    @ApiOperation(value = "导出预售资产", notes = "导出预售资产", httpMethod = "POST")
    @PostMapping("/exportStorePresaleAssets")
    public Response<String> exportStorePresaleAssets(@RequestBody QueryPresaleAssetsPageIn queryPresaleAssetsPageIn) {
        queryPresaleAssetsPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDisPresaleAssetsService.exportStorePresaleAssets(queryPresaleAssetsPageIn));
   }

    /**
     * 分页查询预售资产详情
     *
     * @param queryPresaleAssetsDetailPageIn
     * @return
     */
    @ApiOperation(value = "分页查询预售资产详情", notes = "查询预售资产详情", httpMethod = "POST")
    @PostMapping("/findPresaleAssetsDetailForPage")
    public Response<DisStorePresaleAssetsDetailInfoOut> findPresaleAssetsDetailForPage(@RequestBody QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn) {
        OrdDisPresaleAssets ordDisPresaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsById(queryPresaleAssetsDetailPageIn.getAssetsId());
        if (Objects.isNull(ordDisPresaleAssets)) {
            return Response.error("不存在的门店资产");
        }
        queryPresaleAssetsDetailPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<OrdDisPresaleAssetsDetailOut> page = ordDisPresaleAssetsDetailService.findPresaleAssetsDetailForPage(queryPresaleAssetsDetailPageIn);
        DisStorePresaleAssetsDetailInfoOut disStorePresaleAssetsDetailInfoOut = new DisStorePresaleAssetsDetailInfoOut();
        disStorePresaleAssetsDetailInfoOut.setStoreCode(ordDisPresaleAssets.getStoreCode());
        disStorePresaleAssetsDetailInfoOut.setStoreName(ordDisPresaleAssets.getStoreName());
        disStorePresaleAssetsDetailInfoOut.setPageData(page);
        return Response.data(disStorePresaleAssetsDetailInfoOut);
    }

    // /**
    //  * 导出预售资产详情
    //  */
    // @ApiOperation(value = "导出预售资产详情", notes = "导出预售资产详情", httpMethod = "POST")
    // @PostMapping("/exportPresaleAssetsDetail")
    // public Response<String> exportPresaleAssetsDetail(@RequestBody QueryPresaleAssetsDetailPageIn queryPresaleAssetsPageIn) {
    //     return Response.data(ordDisPresaleAssetsDetailService.exportPresaleAssetsDetail(queryPresaleAssetsPageIn));
    // }

    /**
     * 查询门店预售资产（下配销订货单用）
     *
     * @param storeCode
     * @return
     */
    @ApiOperation(value = "查询门店预售资产（下配销订货单用）", notes = "查询门店预售资产（下配销订货单用）", httpMethod = "GET")
    @GetMapping("/findStorePresaleAssetsInfoForApp")
    public Response<List<PresaleAssetsForAppOut>> findStorePresaleAssetsInfoForApp(@RequestParam String storeCode) {
        List<PresaleAssetsForAppOut> list = ordDisPresaleAssetsDetailService.findStorePresaleAssetsInfo(storeCode);
        return Response.data(list);
    }
}
