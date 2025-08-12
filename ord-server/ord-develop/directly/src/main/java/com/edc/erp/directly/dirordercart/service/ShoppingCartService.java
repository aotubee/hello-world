package com.edc.erp.directly.dirordercart.service;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.directly.dirordercart.entity.OrdDirOrderCart;
import com.edc.erp.directly.dirordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.directly.dirordercart.model.in.DirOperationShoppingCartIn;
import com.edc.erp.directly.dirordercart.model.out.DirCalculationShoppingCartOut;
import com.edc.erp.directly.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.directly.distribution.model.out.OrderCartOut;
import com.edc.erp.directly.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.directly.model.out.OrderCycleDeliveryOut;
import com.edc.plugins.common.response.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-02 09:32
 */
public interface ShoppingCartService {


    /**
     * 删除购物车
     *
     * @param deleteOrderCartInList
     * @param storeCode
     * @param bizOrgCode
     */
    void deleteOrderCart(List<DeleteOrderCartIn> deleteOrderCartInList, String storeCode, String bizOrgCode);


    Response<List<OrderCycleHeaderOut>> updateShoppingCart(List<CreateOrderSkuIn> shoppingCartInList, AppUserOut appUserOut, Boolean isCopyOrder);

    /**
     * 获取购物车订单周期类型
     *
     * @param storeAppUserOut
     * @param orderCartOutList
     * @param sourceCode
     * @param isSubmit
     * @return
     */
    List<OrderCycleHeaderOut> initOrderCycleHeaderOutMap(AppUserOut storeAppUserOut, List<OrderCartOut> orderCartOutList, String sourceCode, boolean isSubmit);

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

    List<OrderCycleHeaderOut> findMyOrderCartList(AppUserOut appUserOut);

    void addOrUpdateOrderCartGoods(OrdDirOrderCart ordDirOrderCart, BigDecimal quantity, String loginUsername, String bizOrgCode, JSONObject redisCartObject);

    DirCalculationShoppingCartOut calculationShoppingCartAmount(List<DirOperationShoppingCartIn> dirOperationShoppingCartInList, AppUserOut appUserOut);

    /**
     *
     *
     * @param skuCode
     * @param bizOrgCode
     * @param targetDateTime
     * @return
     */
    FlashSaleCheckOut isCanBuyByDistributionOrder(String skuCode, String bizOrgCode, LocalDateTime targetDateTime);

}
