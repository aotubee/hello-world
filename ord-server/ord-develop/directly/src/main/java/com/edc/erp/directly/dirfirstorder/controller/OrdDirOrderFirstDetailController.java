package com.edc.erp.directly.dirfirstorder.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirDeleteFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.SaveDirFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.out.DirFirstOrderCheckOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDetailService;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstService;
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
 * 铺货单明细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderFirstDetail")
@Api(value = "ordDirOrderFirstDetail", tags = "铺货单明细表模块")
public class OrdDirOrderFirstDetailController {

    private final OrdDirOrderFirstDetailService ordDirOrderFirstDetailService;

    private final OrdDirOrderFirstService ordDirOrderFirstService;

    private final StoreCenterService storeCenterService;

    @ApiOperation(value = "批量导入铺货单明细", notes = "", httpMethod = "GET")
    @GetMapping("/import")
    public Response<List<OrdDirOrderFirstDetailOut>> importDirReplenishmentOrderDetail(@RequestParam("fileId") String fileId,
                                                                                       @RequestParam("storeCode") String storeCode,
                                                                                       @RequestParam("goodsCodes") List<String> goodsCodes) {
        return ordDirOrderFirstDetailService.importFirstOrderDetail(fileId, storeCode, goodsCodes);
    }

    @ApiOperation(value = "导出铺货单明细列表", notes = "导出铺货单明细列表", httpMethod = "GET")
    @GetMapping("/exportFirstOrderDetail")
    public Response<String> exportDirDirFirstOrderDetail(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn) {
        if (Objects.isNull(ordDirOrderFirstDetailIn.getFirstOrderId())) {
            return Response.error("导出铺货单明细列表必须传递铺货单ID");
        }
        log.info("开始执行/ord/ordDirOrderFirstDetail/exportFirstOrderDetail");
        ordDirOrderFirstDetailIn.setPageNum(NumberUtil.INTEGER_ZERO);
        ordDirOrderFirstDetailIn.setPageSize(NumberUtil.INTEGER_ZERO);
        Page<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetailPage = ordDirOrderFirstService.findOrdDirOrderFirstDetailPage(ordDirOrderFirstDetailIn, UserUtil.getBizOrgCode());
        List<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetails = ordDirOrderFirstDetailPage.getList();
        if (CollectionUtils.isEmpty(ordDirOrderFirstDetails)) {
            return Response.success("没有可导出的数据");
        }
        String privateUrl = ordDirOrderFirstDetailService.exportFirstOrderDetail(ordDirOrderFirstDetailIn, ordDirOrderFirstDetails);
        log.info("结束执行/ord/ordDirOrderFirstDetail/exportFirstOrderDetail");
        return Response.data(privateUrl);
    }

    @ApiOperation(value = "增加铺货明细行", notes = "增加铺货明细行", httpMethod = "POST")
    @PostMapping("/createFirstDetailList")
    public Response<Long> createFirstDetailList(@RequestBody SaveDirFirstDetailIn saveDirFirstDetailIn) {
        String loginUsername = UserUtil.getUserName();
        if (Objects.isNull(saveDirFirstDetailIn.getFirstOrderId())) {
            if (StringUtils.isBlank(saveDirFirstDetailIn.getStoreCode())) {
                return Response.error("需要指定门店");
            }
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(saveDirFirstDetailIn.getStoreCode());
            if (Objects.isNull(storeOut)) {
                return Response.error("门店不存在");
            }
            OrdDirOrderFirst ordDirOrderFirst = new OrdDirOrderFirst();
            ordDirOrderFirst.setStoreCode(storeOut.getStoreCode());
            ordDirOrderFirst.setStoreName(storeOut.getStoreName());
            ordDirOrderFirst.setTotalNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setTotalAmount(BigDecimal.ZERO);
            ordDirOrderFirst.setGoodsNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDirOrderFirst.setCreator(loginUsername);
            ordDirOrderFirst.setUpdater(loginUsername);
            Long firstOrderId = ordDirOrderFirstService.createDirFirstOrder(ordDirOrderFirst);
            saveDirFirstDetailIn.setFirstOrderId(firstOrderId);
        }
        Response<DirFirstOrderCheckOut> response = ordDirOrderFirstDetailService.checkFirstForSaveDetail(saveDirFirstDetailIn, loginUsername);
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        DirFirstOrderCheckOut dirFirstOrderCheckOut = response.getData();
        ordDirOrderFirstDetailService.saveImportFirstOrderDetail(dirFirstOrderCheckOut);
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        if (response.isSuccess() && CollectionUtils.isNotEmpty(dirFirstOrderCheckOut.getErrorResultList())) {
            String goodsErrorMsg = dirFirstOrderCheckOut.getErrorResultList().stream()
                    .map(errorResult -> errorResult.getGoodsCode() + SystemConstant.SHORT_LINE + errorResult.getErrorMessage())
                    .collect(Collectors.joining(SystemConstant.COMMA));
            errorJoiner.add(goodsErrorMsg);
        }
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.data(saveDirFirstDetailIn.getFirstOrderId(), errorJoiner.toString());
        } else {
            return Response.data(saveDirFirstDetailIn.getFirstOrderId());
        }
    }

    @ApiOperation(value = "删除铺货单明细", notes = "删除铺货单明细", httpMethod = "GET")
    @PostMapping("/deleteDetailById")
    public Response<String> deleteDetailById(@RequestBody OrdDirDeleteFirstOrderDetailIn deleteFirstOrderDetailIn) {
        if (Objects.isNull(deleteFirstOrderDetailIn.getFirstOrderId()) || Objects.isNull(deleteFirstOrderDetailIn.getId())) {
            return Response.error("请指定要删除的商品");
        }
        return ordDirOrderFirstDetailService.deleteDetail(deleteFirstOrderDetailIn);
    }
}
