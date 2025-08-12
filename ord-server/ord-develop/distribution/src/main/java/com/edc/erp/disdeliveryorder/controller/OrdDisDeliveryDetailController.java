//package com.edc.erp.disdeliveryorder.controller;
//
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
//import com.edc.erp.model.out.WarehouseInfoOut;
//import com.edc.plugins.common.response.Response;
//import com.edc.uc.authority.util.UserUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//
///**
// * <p>
// * 配销单详情表 前端控制器
// * </p>
// *
// * @author weichao
// * @since 2022-10-10 19:48:19
// */
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/ord/ordDisDeliveryDetail")
//@Api(value = "ordDisDeliveryDetail", tags = "配销单详情表模块")
//public class OrdDisDeliveryDetailController {
//
//    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;
//
//    /***
//     * 查询仓储与仓位信息(二级联动)
//     * @return
//     */
//    @ApiOperation(value = "查询仓储与仓位信息(二级联动)",notes = "查询仓储与仓位信息(二级联动)")
//    @GetMapping("/findDisDeliveryStockInfo")
//    public Response<List<WarehouseInfoOut>> findDisDeliveryStockInfo(){
//        //业务组织
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        List<WarehouseInfoOut> warehouseStockInfos = ordDisDeliveryDetailService.findDisDeliveryStockInfo(bizOrgCode);
//        return Response.data(warehouseStockInfos);
//    }
//}
