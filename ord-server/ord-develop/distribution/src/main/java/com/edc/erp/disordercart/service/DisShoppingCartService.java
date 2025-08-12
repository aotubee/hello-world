package com.edc.erp.disordercart.service;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.disordercart.entity.OrdDisOrderCart;
import com.edc.erp.disordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.disordercart.model.in.DisOperationShoppingCartIn;
import com.edc.erp.disordercart.model.out.CalculationShoppingCartOut;
import com.edc.erp.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.distribution.model.out.OrderCartOut;
import com.edc.erp.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.model.out.OrderCycleDeliveryOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-02 09:32
 */
public interface DisShoppingCartService extends BaseService<OrdDisOrderCart> {


    /**
     * 删除购物车
     *
     * @param deleteOrderCartInList
     * @param storeCode
     * @param bizOrgCode
     */
    void deleteOrderCart(List<DeleteOrderCartIn> deleteOrderCartInList, String storeCode, String bizOrgCode);


    /**
     * 新增或者更新购物车
     *
     * @param ordDisOrderCart 购物车对象
     * @param quantity        数量
     * @param loginUsername   登录加盟商
     * @param orgCode         公司代码
     * @param redisCartObject redis购物车集合
     */
    void addOrUpdateOrderCartGoods(OrdDisOrderCart ordDisOrderCart, BigDecimal quantity, String loginUsername, String orgCode, JSONObject redisCartObject);

    /**
     * 获取购物车订单周期类型
     *
     * @param storeAppUserOut
     * @param orderCartOutList
     * @param sourceCode
     * @param requestType 1 刷新购物车 2 更新 3 计算 4 下单
     * @return
     */
    List<OrderCycleHeaderOut> initOrderCycleHeaderOutMap(AppUserOut storeAppUserOut, List<OrderCartOut> orderCartOutList, String sourceCode, int requestType);

    /**
     * 获取门店运送截止时间
     *
     * @param storeDeliveryType
     * @param deliveryDailyCycle
     * @param truncationTimePoint
     * @return
     */
    String getStoreDeliveryTruncationTime(String storeDeliveryType, String deliveryDailyCycle, String truncationTimePoint);

    /**
     * 判断是否可以加购
     *
     * @param skuCode
     * @param bizOrgCode
     * @return
     */
    FlashSaleCheckOut isCanBuyFlashSaleGoods(String skuCode, String bizOrgCode);

    /**
     * 获取门店配送周期类型
     *
     * @param storeLogistics
     * @param orderPeriod
     * @return
     */
    OrderCycleDeliveryOut getStoreDeliveryType(StoreLogisticsOut storeLogistics, String orderPeriod);

    OrderCycleDeliveryOut getStoreDeliveryInfoType(StoreDelivery storeDelivery);

    /**
     * 核算购物车金额
     *
     * @param disOperationShoppingCartInList
     * @param appUserOut
     * @return
     */
    CalculationShoppingCartOut calculationShoppingCartAmount(List<DisOperationShoppingCartIn> disOperationShoppingCartInList, AppUserOut appUserOut);

    /**
     * 更新购物车
     *
     * @param shoppingCartInList
     * @param appUserOut
     * @return
     */
    Response<List<OrderCycleHeaderOut>> updateShoppingCart(List<CreateOrderSkuIn> shoppingCartInList, AppUserOut appUserOut, Boolean isCopyOrder);

    List<OrderCycleHeaderOut> findMyOrderCartList(AppUserOut appUserOut);

    FlashSaleCheckOut isCanBuyByDistributionOrder(String skuCode, String bizOrgCode, LocalDateTime targetDateTime);
}
