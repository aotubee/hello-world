package com.edc.erp.common.service;

import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.goods.QueryCheckUpperLowerGoodsIn;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.erp.common.model.out.GoodsDisSpecOut;
import com.edc.erp.common.model.out.goods.*;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.erp.common.model.vo.ActivityAdditionVO;
import com.edc.plugins.common.response.Response;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品信息出参
 *
 * @author wuke
 */
public interface OrderGoodsServer {
    /**
     * 根据商品代码查询商品状态是否合法
     *
     * @param goodsCode
     * @param bizOrgCode
     * @param storeCode
     * @param sourceCode
     * @return
     */
    boolean countForBusGateBySkuCode(String goodsCode, String bizOrgCode, String storeCode, String sourceCode);

    /**
     * 获取商品信息
     *
     * @param orderGoodsIn
     * @return
     */
    OrderGoodsOut getStoreOrderGoodsByCache(OrderGoodsIn orderGoodsIn);

    /**
     * 获取商品信息
     *
     * @param orderGoodsIn
     * @return
     */
    OrderGoodsOut getStoreOrderGoods(OrderGoodsIn orderGoodsIn);

    /**
     * 查询商品code集合（有业务）
     *
     * @param orderGoodsIn
     * @return
     */
    List<String> findBusinessGoodsCodeList(OrderGoodsIn orderGoodsIn);

    /**
     * 查询商品code（无业务）
     *
     * @param orderGoodsIn
     * @return
     */
    OrderGoodsOut getOrderGoods(OrderGoodsIn orderGoodsIn);

    /**
     * 根据品类code获取品类信息
     *
     * @param code
     * @param bizOrgCode
     * @return
     */
    OrgSortOut getByCode(String code, String bizOrgCode);


    /**
     * 根据组织和code获取仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    StockTransInfoOut getTransInfo(String stockCode, String bizOrgCode);

    /**
     * 获取商品基本信息
     *
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    OrgGoodsTransInfo getGoodsOut(String goodsCode, String bizOrgCode);

    /**
     * 查询商品信息及允许业务状态信息
     *
     * @param orderGoodsIn 商品入参查询类
     * @return
     */
    OrderGoodsOut getSwitchGoodsInfo(OrderGoodsIn orderGoodsIn);

    /**
     * 查询商品物流属性（散件/整件/高值）
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    Integer getGoodsLogisticsByGoodsCode(String goodsCode, String bizOrgCode);

    /**
     * 查询商品活动信息
     * @param bizOrgCode
     * @param storeCode
     * @param goodsCode
     * @return
     */
    ActivityAdditionVO getActivityByGoodsAndStore(String bizOrgCode, String storeCode, String goodsCode);

    /**
     * 查询商品可订货时间段
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    List<FlashSaleWeekOut> findFlashSaleWeekOutList(String goodsCode, String bizOrgCode);

    /**
     * 根据条件获取零售价格
     * @param bizOrgCode
     * @param storeCode
     * @param goodsCode
     * @return
     */
    BigDecimal getSellPrice(@Param("bizOrgCode") String bizOrgCode, @Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode);

    List<OrderGoodsOut> findGoodsInfoByStoreAndGoodsCodes(OrderGoodsIn goodsIn);

    Response<List<OrderGoodsOut>> findGoodsInfoForDistributionImport(OrderGoodsIn goodsIn);

    OrderGoodsOut getStoreOrderGoodsForBackReturn(OrderGoodsIn orderGoodsIn);

    List<StandardGoodsInfoOut> findAllByGoodsCodeList(List<String> goodsCodeList);

    List<GoodsForOrdOut> findGoodsInfos(OrdQueryGoodsIn ordQueryGoodsIn);

//    List<CheckUpperLowerGoodsOut> findGoodsInfoForCheckUppLower(QueryCheckUpperLowerGoodsIn queryCheckUpperLowerGoodsIn);

    List<OrderGoodsOut> findForUpDownLimit(OrdQueryGoodsIn ordQueryGoodsIn);

    GoodsDisSpecOut getGoodsDisSpecOutByOrg(String goodsCode, String bizOrgCode);

    List<GoodsValidityPeriodInfo> findValidityPeriodInfos(List<String> goodsCodes, String bizOrgCode);

    List<GoodsDisSpecOut> findGoodsDisSpecOutByOrg(OrdQueryGoodsIn ordQueryGoodsIn);

    OrderGoodsOut getStoreGoodsNoType(OrderGoodsIn orderGoodsIn);
}