package com.edc.erp.wholesale.shipment.controller;

import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * <p>
 * 批发出货单明细 前端控制器
 * </p>
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/wholesaleShipmentDetail")
@Api(value = "wholesaleShipmentDetail", tags = "批发出货单明细模块")
public class WholesaleShipmentDetailController {

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    /**
     * 分页查询出货单明细
     * @param queryShipmentDetailIn 出货单明细查询入参
     * @return
     */
    @ApiOperation(value = "分页查询出货单明细",notes = "分页查询出货单明细")
    @GetMapping("/findByPage")
    public Response<Page<WholesaleShipmentDetailOut>> findByPage(QueryShipmentDetailIn queryShipmentDetailIn){
        Page<WholesaleShipmentDetailOut> detailOutPage = wholesaleShipmentDetailService.findByPage(queryShipmentDetailIn);
        return Response.data(detailOutPage);
    }
}
