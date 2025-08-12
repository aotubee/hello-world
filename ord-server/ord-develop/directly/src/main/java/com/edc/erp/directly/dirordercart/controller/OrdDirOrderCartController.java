package com.edc.erp.directly.dirordercart.controller;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.SourceTypeEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.directly.dirordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.directly.dirordercart.model.in.DirOperationShoppingCartIn;
import com.edc.erp.directly.dirordercart.model.out.DirCalculationShoppingCartOut;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.directly.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * 直营配货购物车 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-17 18:58:04
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderCart")
@Api(value = "ordDirOrderCart", tags = "直营配货购物车模块")
public class OrdDirOrderCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    private DirOrderConfigHandle orderConfigHandle;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @ApiOperation(value = "（新）获取购物车列表", notes = "（新）获取购物车列表", httpMethod = "GET")
    @GetMapping("/findMyOrderCartList")
    public Response<List<OrderCycleHeaderOut>> findMyOrderCartList(HttpServletRequest request) {
        String userName = UserUtil.getUserName(request);
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED,"不存在的用户");
        }
        List<OrderCycleHeaderOut> orderCycleHeaderOuts = shoppingCartService.findMyOrderCartList(appUserOut);
        return Response.data(orderCycleHeaderOuts);
    }

    @ApiOperation(value = "批量删除购物车记录", notes = "批量删除购物车记录", httpMethod = "POST")
    @PostMapping("/batchDeleteOrderCart")
    public Response batchDeleteOrderCart(@RequestBody List<DeleteOrderCartIn> deleteOrderCartInList, HttpServletRequest request) {
        String userName = UserUtil.getUserName(request);
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED,"不存在的用户");
        }
        String bizOrgCode = UserUtil.getBizOrgCode(request);
        shoppingCartService.deleteOrderCart(deleteOrderCartInList, appUserOut.getStoreCode(), bizOrgCode);
        return Response.success("删除成功");
    }

    @ApiOperation(value = "核算购物车金额", notes = "核算购物车金额", httpMethod = "POST")
    @PostMapping("/calculationOrderCartAmount")
    public Response<DirCalculationShoppingCartOut> calculationShoppingCartAmount(@RequestBody List<DirOperationShoppingCartIn> dirOperationShoppingCartInList, HttpServletRequest request) {
        String userName = UserUtil.getUserName(request);
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED,"不存在的用户");
        }
        return Response.data(shoppingCartService.calculationShoppingCartAmount(dirOperationShoppingCartInList, appUserOut));
    }

    @ApiOperation(value = "更新购物车记录", notes = "更新购物车记录", httpMethod = "POST")
    @PostMapping("/updateOrderCartElement")
    public Response<List<OrderCycleHeaderOut>> updateOrderCartElement(@RequestBody List<CreateOrderSkuIn> shoppingCartInList, HttpServletRequest request) {
        String userName = UserUtil.getUserName(request);
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED,"不存在的用户");
        }
        return shoppingCartService.updateShoppingCart(shoppingCartInList, appUserOut, false);
    }

    @ApiOperation(value = "是否可以加购", notes = "是否可以加购", httpMethod = "GET")
    @GetMapping("/isCanBuyFlashSaleGoods")
    public Response<FlashSaleCheckOut> isCanBuyFlashSaleGoods(String skuCode, HttpServletRequest request) {
        String bizOrgCode = UserUtil.getBizOrgCode(request);
        return Response.data(shoppingCartService.isCanBuyFlashSaleGoods(skuCode, bizOrgCode));
    }

    @ApiOperation(value = "测试下单配置", notes = "测试下单配置", httpMethod = "POST")
    @PostMapping("/testCombinationType")
    public Response testCombinationType(@RequestBody OrderGoodsIn orderGoodsIn){
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setStoreCode(appUserOut.getStoreCode());
//        orderGoodsIn.setGoodsCode(createOrderSkuIn.getGoodsCode());
//        orderGoodsIn.setBizOrgCode(appUserOut.getBizOrgCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            return Response.error("不能操作不存在的商品" + orderGoodsIn.getGoodsCode());
        }
        Objects.requireNonNull(orderGoodsOut.getStockCode(), orderGoodsIn.getGoodsCode() + "仓位代码为空");
        List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), orderGoodsOut.getGoodsType());
        String bizOrgCode = orderGoodsIn.getBizOrgCode();
        //rpc 查询订单类型配置
        DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(orderGoodsIn.getStoreCode(), bizOrgCode, combinationTypeCodeList);
        if(Objects.nonNull(dirOrderTypeConfig)){
            return Response.data(JSONObject.toJSONString(dirOrderTypeConfig));
        }
        return Response.success("未找到.");
    }
}

