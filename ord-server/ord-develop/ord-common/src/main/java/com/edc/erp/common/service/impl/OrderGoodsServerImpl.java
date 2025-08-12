package com.edc.erp.common.service.impl;

import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.mapper.OrgGoodsInfoMapper;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.goods.FlashSaleWeekIn;
import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.erp.common.model.out.GoodsDisSpecOut;
import com.edc.erp.common.model.out.goods.*;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.vo.ActivityAdditionVO;
import com.edc.erp.common.rpc.OrderGoodsClient;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.plugins.cache.CacheConst;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 获取商品信息
 *
 * @author weichao
 */
@Slf4j
@Service
public class OrderGoodsServerImpl implements OrderGoodsServer {
    @Resource
    private OrderGoodsClient orderGoodsClient;

    @Resource
    private StoreCenterService storeCenterService;

    @Autowired
    private OrgGoodsInfoMapper orgGoodsInfoMapper;

    /**
     * 根据商品代码查询商品状态是否合法
     *
     * @param goodsCode
     * @param bizOrgCode
     * @param storeCode
     * @param sourceCode
     * @return
     */
    @Override
    public boolean countForBusGateBySkuCode(String goodsCode, String bizOrgCode, String storeCode, String sourceCode) {
        StoreInfo storeInfo = storeCenterService.getStoreByCode(storeCode, bizOrgCode);
        if (Objects.isNull(storeInfo)) {
            throw new BusinessException("查询门店信息出错，请刷新重试！");
        }
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(sourceCode));
        OrderGoodsOut orderGoodsOut = this.getStoreOrderGoods(orderGoodsIn);
        return Objects.nonNull(orderGoodsOut);

    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_store_biz_goods",
            key = "#orderGoodsIn.bizOrgCode+ ':' + #orderGoodsIn.storeCode + '_' + #orderGoodsIn.goodsCode + '_' + #orderGoodsIn.businessType + '_' + #orderGoodsIn.sourceCode", unless = "#result == null")
    public OrderGoodsOut getStoreOrderGoodsByCache(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getStoreOrderGoods(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public OrderGoodsOut getStoreOrderGoods(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getStoreOrderGoods(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
//            log.error("getStoreOrderGoods-------入参{}--------返回消息{}", JSONObject.toJSON(orderGoodsIn), response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<String> findBusinessGoodsCodeList(OrderGoodsIn orderGoodsIn) {
        Response<List<String>> response = orderGoodsClient.findBusinessGoodsCodeList(orderGoodsIn);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public OrderGoodsOut getOrderGoods(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getOrderGoods(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_org_sort_out",
            key = "#bizOrgCode+ ':' + #code", unless = "#result == null")
    public OrgSortOut getByCode(String code, String bizOrgCode) {
        Response<OrgSortOut> response = orderGoodsClient.getByCode(code, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_stock_trans_info",
            key = "#bizOrgCode+ ':' + #stockCode", unless = "#result == null")
    public StockTransInfoOut getTransInfo(String stockCode, String bizOrgCode) {
        Response<StockTransInfoOut> response = orderGoodsClient.getTransInfo(stockCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public OrgGoodsTransInfo getGoodsOut(String goodsCode, String bizOrgCode) {
        Response<OrgGoodsTransInfo> response = orderGoodsClient.getGoodsOut(goodsCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 查询商品信息及允许业务状态信息
     *
     * @param orderGoodsIn 商品入参查询类
     * @return
     */
    @Override
    public OrderGoodsOut getSwitchGoodsInfo(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getSwitchGoodsInfo(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public Integer getGoodsLogisticsByGoodsCode(String goodsCode, String bizOrgCode) {
        return orgGoodsInfoMapper.getGoodsLogisticsByGoodsCode(goodsCode, bizOrgCode);
    }

    @Override
    public ActivityAdditionVO getActivityByGoodsAndStore(String bizOrgCode, String storeCode, String goodsCode) {
        try {
            Response<ActivityAdditionVO> response = orderGoodsClient.getActivityByGoodsAndStore(bizOrgCode, storeCode, goodsCode);
            if (!response.isSuccess()) {
                log.error("远程调用查询商品活动信息时出错,商品代码是{},门店代码是{},错误信息是{}", goodsCode, storeCode, response.getMessage());
                return null;
            }
            return response.getData();
        } catch (Exception e) {
            log.error("远程调用查询商品活动信息时出错,商品代码是{},门店代码是{}", goodsCode, storeCode, e);
            return null;
        }
    }

    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "goods_flash_sale_week",
            key = "#bizOrgCode+ ':' + #goodsCode", unless = "#result == null")
    @Override
    public List<FlashSaleWeekOut> findFlashSaleWeekOutList(String goodsCode, String bizOrgCode) {
        Response<List<FlashSaleWeekOut>> listResponse = orderGoodsClient.findFlashSaleWeekOutList(new FlashSaleWeekIn(goodsCode, bizOrgCode));
        if (!listResponse.isSuccess()) {
            throw new BusinessException(goodsCode + "商品可订货时间段获取失败,请稍后重试!");
        }
        List<FlashSaleWeekOut> list = listResponse.getData();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }

    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "store_goods_sell_price_by_dis",
            key = "#bizOrgCode+ ':' + #storeCode + '_' + #goodsCode", unless = "#result == null")
    @Override
    public BigDecimal getSellPrice(String bizOrgCode, String storeCode, String goodsCode) {
        return orgGoodsInfoMapper.getSellPrice(bizOrgCode, storeCode, goodsCode);
    }

    @Override
    public List<OrderGoodsOut> findGoodsInfoByStoreAndGoodsCodes(OrderGoodsIn goodsIn) {
        Response<List<OrderGoodsOut>> response = orderGoodsClient.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public Response<List<OrderGoodsOut>> findGoodsInfoForDistributionImport(OrderGoodsIn goodsIn) {
        Response<List<OrderGoodsOut>> response = orderGoodsClient.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
        return response;
    }

    @Override
    public OrderGoodsOut getStoreOrderGoodsForBackReturn(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<StandardGoodsInfoOut> findAllByGoodsCodeList(List<String> goodsCodeList) {
        Response<List<StandardGoodsInfoOut>> response = orderGoodsClient.findAllByGoodsCodeList(goodsCodeList);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return Lists.newArrayList();
        }
        return response.getData();
    }

    @Override
    public List<GoodsForOrdOut> findGoodsInfos(OrdQueryGoodsIn ordQueryGoodsIn) {
        Response<List<GoodsForOrdOut>> response = orderGoodsClient.findGoodsInfos(ordQueryGoodsIn);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

//    @Override
//    public List<CheckUpperLowerGoodsOut> findGoodsInfoForCheckUppLower(QueryCheckUpperLowerGoodsIn queryCheckUpperLowerGoodsIn) {
//        Response<List<CheckUpperLowerGoodsOut>> response = orderGoodsClient.findGoodsInfoForCheckUppLower(queryCheckUpperLowerGoodsIn);
//        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
//            log.error(response.getMessage());
//            return null;
//        }
//        return response.getData();
//    }

    @Override
    public List<OrderGoodsOut> findForUpDownLimit(OrdQueryGoodsIn ordQueryGoodsIn) {
        Response<List<OrderGoodsOut>> response = orderGoodsClient.findForUpDownLimit(ordQueryGoodsIn);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<GoodsValidityPeriodInfo> findValidityPeriodInfos(List<String> goodsCodes, String bizOrgCode) {
        Response<List<GoodsValidityPeriodInfo>> response = orderGoodsClient.findValidityPeriodInfos(goodsCodes, bizOrgCode);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public GoodsDisSpecOut getGoodsDisSpecOutByOrg(String goodsCode, String bizOrgCode) {
        Response<GoodsDisSpecOut> response = orderGoodsClient.getGoodsDisSpecOutByOrg(goodsCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<GoodsDisSpecOut> findGoodsDisSpecOutByOrg(OrdQueryGoodsIn ordQueryGoodsIn) {
        Response<List<GoodsDisSpecOut>> response = orderGoodsClient.findGoodsDisSpecOutByOrg(ordQueryGoodsIn);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public OrderGoodsOut getStoreGoodsNoType(OrderGoodsIn orderGoodsIn) {
        Response<OrderGoodsOut> response = orderGoodsClient.getStoreGoodsNoType(orderGoodsIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
