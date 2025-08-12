package com.edc.erp.disfirstorder.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisDeleteFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstDetailIn;
import com.edc.erp.disfirstorder.model.in.SaveDisFirstDetailIn;
import com.edc.erp.disfirstorder.model.out.DisFirstOrderCheckOut;
import com.edc.erp.disfirstorder.model.out.OrdDisOrderFirstDetailOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * <p>
 * 配销铺货单明细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-10 16:18:10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderFirstDetail")
@Api(value = "ordDisOrderFirstDetail", tags = "配销铺货单明细表模块")
public class OrdDisOrderFirstDetailController {

    private final OrdDisOrderFirstDetailService ordDisOrderFirstDetailService;

    private final OrdDisOrderFirstService ordDisOrderFirstService;

    private final StoreCenterService storeCenterService;

    @ApiOperation(value = "获取首单铺货订单明细列表", notes = "", httpMethod = "GET")
    @GetMapping("/findByPage")
    public Response<Page<OrdDisOrderFirstDetailOut>> findOrdDisOrderFirstDetailPage(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        Page<OrdDisOrderFirstDetailOut> firstOrderDetailOutPage = ordDisOrderFirstDetailService.findOrdDisOrderFirstDetailPage(ordDisOrderFirstDetailIn, bizOrgCode);
        return Response.data(firstOrderDetailOutPage);
    }

    @ApiOperation(value = "保存铺货单及明细", notes = "", httpMethod = "POST")
    @PostMapping(value = "/save")
    public Response<OrdDisOrderFirst> saveFirstOrderDetail(@RequestBody InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        // 校验明细数据
        Response response = ordDisOrderFirstDetailService.checkRepeatParam(insertFirstOrderDetailIn, bizOrgCode);
        if (!response.isSuccess()) {
            return response;
        }
        // 删除并批量新增数据, 返回单据ID
        return ordDisOrderFirstDetailService.saveFirstOrderDetail(insertFirstOrderDetailIn);
    }

    @ApiOperation(value = "导出铺货单明细列表", notes = "导出铺货单明细列表", httpMethod = "GET")
    @GetMapping("/exportFirstOrderDetail")
    public Response<String> exportFirstOrderDetail(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn) {
        if (Objects.isNull(ordDisOrderFirstDetailIn.getFirstOrderId())) {
            return Response.error("导出铺货单明细列表必须传递铺货单ID");
        }
        log.info("开始执行/ord/ordDisOrderFirstDetail/exportFirstOrderDetail");
        String privateUrl = ordDisOrderFirstDetailService.exportFirstOrderDetail(ordDisOrderFirstDetailIn);
        log.info("结束执行/ord/ordDisOrderFirstDetail/exportFirstOrderDetail");
        return Response.data(privateUrl);
    }

    @ApiOperation(value = "批量导入铺货单明细", notes = "", httpMethod = "GET")
    @GetMapping("/import")
    public Response<List<OrdDisOrderFirstDetailOut>> importReplenishmentOrderDetail(@RequestParam("fileId") String fileId,
                                                                                    @RequestParam("storeCode") String storeCode,
                                                                                    @RequestParam("goodsCodes") List<String> goodsCodes) {
        return ordDisOrderFirstDetailService.importFirstOrderDetail(fileId, storeCode, goodsCodes);
    }

    @ApiOperation(value = "增加铺货明细行", notes = "增加铺货明细行", httpMethod = "POST")
    @PostMapping("/createFirstDetailList")
    public Response<Long> createFirstDetailList(@RequestBody SaveDisFirstDetailIn saveDisFirstDetailIn) {
        String loginUsername = UserUtil.getUserName();
        if (Objects.isNull(saveDisFirstDetailIn.getFirstOrderId())) {
            if (StringUtils.isBlank(saveDisFirstDetailIn.getStoreCode())) {
                return Response.error("需要指定门店");
            }
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(saveDisFirstDetailIn.getStoreCode());
            if (Objects.isNull(storeOut)) {
                return Response.error("门店不存在");
            }
            OrdDisOrderFirst ordDirOrderFirst = new OrdDisOrderFirst();
            ordDirOrderFirst.setStoreCode(storeOut.getStoreCode());
            ordDirOrderFirst.setStoreName(storeOut.getStoreName());
            ordDirOrderFirst.setTotalNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setTotalAmount(BigDecimal.ZERO);
            ordDirOrderFirst.setGoodsNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDirOrderFirst.setCreator(loginUsername);
            ordDirOrderFirst.setUpdater(loginUsername);
            Long firstOrderId = ordDisOrderFirstService.createDirFirstOrder(ordDirOrderFirst);
            saveDisFirstDetailIn.setFirstOrderId(firstOrderId);
        }
        Response<DisFirstOrderCheckOut> response = ordDisOrderFirstDetailService.checkFirstForSaveDetail(saveDisFirstDetailIn, loginUsername);
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        DisFirstOrderCheckOut dirFirstOrderCheckOut = response.getData();
        ordDisOrderFirstDetailService.saveImportFirstOrderDetail(dirFirstOrderCheckOut);
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        if (response.isSuccess() && CollectionUtils.isNotEmpty(dirFirstOrderCheckOut.getErrorResultList())) {
            String goodsErrorMsg = dirFirstOrderCheckOut.getErrorResultList().stream()
                    .map(errorResult -> errorResult.getGoodsCode() + SystemConstant.SHORT_LINE + errorResult.getErrorMessage())
                    .collect(Collectors.joining(SystemConstant.COMMA));
            errorJoiner.add(goodsErrorMsg);
        }
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.data(saveDisFirstDetailIn.getFirstOrderId(), errorJoiner.toString());
        } else {
            return Response.data(saveDisFirstDetailIn.getFirstOrderId());
        }
    }

    @ApiOperation(value = "删除铺货单明细", notes = "删除铺货单明细", httpMethod = "GET")
    @PostMapping("/deleteDetailById")
    public Response<String> deleteDetailById(@RequestBody OrdDisDeleteFirstOrderDetailIn deleteFirstOrderDetailIn) {
        if (Objects.isNull(deleteFirstOrderDetailIn.getFirstOrderId()) || Objects.isNull(deleteFirstOrderDetailIn.getId())) {
            return Response.error("请指定要删除的商品");
        }
        return ordDisOrderFirstDetailService.deleteDetail(deleteFirstOrderDetailIn);
    }
}
