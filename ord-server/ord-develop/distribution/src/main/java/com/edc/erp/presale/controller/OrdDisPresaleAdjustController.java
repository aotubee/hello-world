package com.edc.erp.presale.controller;

import com.edc.erp.presale.model.in.PresaleAdjustOrderPageIn;
import com.edc.erp.presale.model.in.PresaleAdjustOrderSaveIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderGetOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderPageOut;
import com.edc.erp.presale.service.OrdDisPresaleAdjustOrderService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName OrdDisPresaleAdjustController
 * @Author ZhangYao
 * @CreateTime 2024/8/23 8:43
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disPresaleAdjust")
@Api(value = "ordDisPresaleAdjustController", tags = "预售调整接口")
public class OrdDisPresaleAdjustController {
    private final OrdDisPresaleAdjustOrderService ordDisPresaleAdjustService;

    /**
     * 预售调整单列表分页查询
     */
    @ApiOperation(value = "预售调整单列表分页查询", notes = "预售调整单列表分页查询", httpMethod = "POST")
    @PostMapping("/pageAdjustOrder")
    public Response<Page<PresaleAdjustOrderPageOut>> pageAdjustOrder(@RequestBody PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        return Response.data(ordDisPresaleAdjustService.findPresaleAdjustOrderByPage(presaleAdjustOrderPageIn));
    }

    /**
     * 预售调整单列表数据导出
     */
    @ApiOperation(value = "预售调整单列表数据导出", notes = "预售调整单列表数据导出", httpMethod = "POST")
    @PostMapping("/exportAdjustOrder")
    public Response<String> exportAdjustOrder(@RequestBody PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        return Response.data(ordDisPresaleAdjustService.exportAdjustOrder(presaleAdjustOrderPageIn));
    }

    /**
     * 预售调整单详情商品批量导出
     */
    @ApiOperation(value = "预售调整单详情商品批量导出", notes = "预售调整单详情商品批量导出", httpMethod = "POST")
    @PostMapping("/exportAdjustOrders")
    public Response<String> exportAdjustOrders(@RequestBody PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        return Response.data(ordDisPresaleAdjustService.exportAdjustOrders(presaleAdjustOrderPageIn));
    }

    /**
     * 预售调整单详情
     */
    @ApiOperation(value = "预售调整单详情", notes = "预售调整单详情", httpMethod = "GET")
    @GetMapping("/getAdjustOrderDetail")
    public Response<PresaleAdjustOrderGetOut> getPresaleAdjustOrderDetail(@RequestParam @NotNull Long id) {
        return Response.data(ordDisPresaleAdjustService.getPresaleAdjustOrderDetail(id));
    }
    // 1.预售调整单作废  2.预售调整单审核 3.预售调整单保存 4.预售调整单冲销

    /**
     * 预售调整单作废
     */
    @ApiOperation(value = "预售调整单作废", notes = "预售调整单作废", httpMethod = "GET")
    @GetMapping("/invalidAdjustOrder")
    public Response<String> invalidAdjustOrder(@RequestParam @NotNull Long id) {
        boolean result = ordDisPresaleAdjustService.invalidAdjustOrder(id);
        if (result) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    /**
     * 预售调整单审核
     */
    @ApiOperation(value = "预售调整单审核", notes = "预售调整单审核", httpMethod = "GET")
    @GetMapping("/approveAdjustOrder")
    public Response<String> approveAdjustOrder(@RequestParam @NotNull Long id) {
        boolean result = ordDisPresaleAdjustService.approveAdjustOrder(id);
        if (result) {
            return Response.success("审核成功");
        }
        return Response.error("审核失败");
    }

    /**
     * 预售调整单保存
     */
    @ApiOperation(value = "预售调整单保存", notes = "预售调整单保存", httpMethod = "POST")
    @PostMapping("/saveAdjustOrder")
    public Response<Long> saveAdjustOrder(@RequestBody @Validated PresaleAdjustOrderSaveIn orderSaveIn) {
        return Response.data(ordDisPresaleAdjustService.saveAdjustOrder(orderSaveIn));
    }

    /**
     * 预售调整单冲销
     */
    @ApiOperation(value = "预售调整单冲销", notes = "预售调整单冲销", httpMethod = "GET")
    @GetMapping("/chargeAdjustOrder")
    public Response<String> chargeAdjustOrder(@RequestParam @NotNull Long id) {
        boolean result = ordDisPresaleAdjustService.chargeAdjustOrder(id);
        if (result) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    /**
     * 预售调整单详情商品导出
     */
    @ApiOperation(value = "预售调整单详情商品导出", notes = "预售调整单详情商品导出", httpMethod = "GET")
    @GetMapping("/exportAdjustOrderDetail")
    public Response<String> exportAdjustOrderDetail(@RequestParam @NotNull Long id) {
        return Response.data(ordDisPresaleAdjustService.exportAdjustOrderDetail(id));
    }

    /**
     * 根据门店编码、商品代码查询资产模块该门店商品代码关联的数据
     */
    @ApiOperation(value = "根据门店编码、商品代码查询资产模块该门店商品代码关联的数据", notes = "根据门店编码、商品代码查询资产模块该门店商品代码关联的数据", httpMethod = "GET")
    @GetMapping("/getAssetsDetail")
    public Response<List<OrdDisPresaleAssetsDetailOut>> getAssetsDetail(@RequestParam String storeCode, @RequestParam String goodsCode) {
        return Response.data(ordDisPresaleAdjustService.getStoreGoodsAssetsDetailList(storeCode, goodsCode));
    }

    /**
     * 导入预售调整单
     */
    @ApiOperation(value = "导入预售调整单", notes = "导入预售调整单", httpMethod = "GET")
    @GetMapping("/importAdjustOrder")
    public Response<String> importAdjustOrder(@RequestParam String fileId) {
        return ordDisPresaleAdjustService.importAdjustOrder(fileId);
    }

    /**
     * 导入预售调整单明细
     */
    @ApiOperation(value = "导入预售调整单明细", notes = "导入预售调整单明细", httpMethod = "GET")
    @GetMapping("/importAdjustOrderDetail")
    public Response<List<PresaleAdjustOrderDetailOut>> importAdjustOrderDetail(@RequestParam String fileId, @RequestParam String storeCode, @RequestParam String adjustType) {
        return ordDisPresaleAdjustService.importAdjustOrderDetail(storeCode, adjustType, fileId);
    }
}
