package com.edc.erp.ord.controller;

import cn.hutool.core.date.DateUtil;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : lishaobo
 * @date 2023-04-18 15:32
 */
@Api(tags = "在单量控制器")
@RestController
@RequestMapping("/ord/inTransitQty")
@Slf4j
public class GoodsInTransitQtyController {

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @ApiOperation(value = "获取门店在单量商品信息", notes = "获取门店在单量商品信息", httpMethod = "GET")
    @GetMapping("/findInTransitQtyByStoreAndProperty")
    public Response<List<InOneQtyVO>> findInTransitQtyByStoreAndProperty(@RequestParam @NotBlank String storeCode, @RequestParam @NotBlank String storeProperty) {
        String beginTime = LocalDate.now().minusDays(5) + " 00:00:00";
        String endTime = DateUtil.formatLocalDateTime(LocalDateTime.now());
        List<InOneQtyVO> inOneQtyVOList = null;
        if (StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeProperty)) {
            inOneQtyVOList = ordDirDeliveryService.findInDeliveryOrder(storeCode, beginTime, endTime);
        } else {
            inOneQtyVOList = ordDisDeliveryService.findInDeliveryOrder(storeCode, beginTime, endTime);
        }
        return Response.data(inOneQtyVOList);
    }
}
