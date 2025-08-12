package com.edc.erp.common.rpc;


import com.edc.erp.common.model.in.goods.*;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.erp.common.model.out.GoodsDisSpecOut;
import com.edc.erp.common.model.out.goods.*;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.erp.common.model.vo.ActivityAdditionVO;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 订单商品详细信息接口
 * @author weichao
 */
/*url = "http://192.168.31.89:11020",*/
@FeignClient(name ="mdm",contextId ="OrderGoodsClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface OrderGoodsClient {
    /**
     *门店商品信息出参（有业务类型）
     * @param orderGoodsIn
     * @return
     */
    @PostMapping("/gc/orderGoods/getStoreOrderGoods")
    Response<OrderGoodsOut> getStoreOrderGoods(@RequestBody OrderGoodsIn orderGoodsIn);

    /**
     *  查询商品code集合（有业务）
     *
     * @param orderGoodsIn
     * @return
     */
    @PostMapping("/gc/orderGoods/findBusinessGoodsCodeList")
    Response<List<String>> findBusinessGoodsCodeList(@RequestBody OrderGoodsIn orderGoodsIn);

    /**
     * 查商品基本信息（无业务类型）
     * @param orderGoodsIn
     * @return
     */
    @PostMapping("gc/orderGoods/getOrderGoods")
    Response<OrderGoodsOut> getOrderGoods(@RequestBody OrderGoodsIn orderGoodsIn);

    /**
     * 根据品类code和组织查询运营品类信息
     * @param code
     * @param bizOrgCode
     * @return
     */
    @GetMapping("/gc/orgSort/getByCode")
    @ApiOperation("根据品类code和组织查询运营品类信息")
    Response<OrgSortOut> getByCode(@RequestParam("code")  String code,
                                   @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 根据组织和code获取仓位信息
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据组织和code获取仓位信息", httpMethod = "GET")
    @GetMapping("/logc/stock/getTransInfo")
    Response<StockTransInfoOut> getTransInfo(@RequestParam("stockCode") String stockCode, @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 根据code和组织获取组织商品信息(翻译用)
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据code和组织获取组织商品信息(翻译用)", notes = "获取组织商品基础信息", httpMethod = "GET")
    @GetMapping("/gc/orgGoodsInfo/getTransInfo")
    Response<OrgGoodsTransInfo> getGoodsOut(@RequestParam ("goodsCode") String goodsCode, @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 查询商品信息及允许业务状态信息
     * @param orderGoodsIn 商品入参查询类
     * @return
     */
    @ApiOperation(value = "查询商品信息及允许业务状态信息", notes = "查询商品信息及允许业务状态信息")
    @PostMapping("/gc/orderGoods/getSwitchGoodsInfo")
    Response<OrderGoodsOut> getSwitchGoodsInfo(@RequestBody OrderGoodsIn orderGoodsIn);

    /**
     * 查询商品活动信息
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "查询商品活动信息", notes = "查询商品活动信息", httpMethod = "GET")
    @GetMapping("/gc/orderGoods/getActivityByGoodsAndStore")
    Response<ActivityAdditionVO> getActivityByGoodsAndStore(@RequestParam(value = "bizOrgCode", required = false) String bizOrgCode,
                                                            @RequestParam(value = "storeCode") String storeCode, @RequestParam(value = "goodsCode") String goodsCode);

    /**
     * 查询商品可订货时间段信息
     * @param flashSaleWeekIn
     * @return
     */
    @ApiOperation(value = "查询商品活动信息", notes = "查询商品活动信息")
    @PostMapping("/gc/goodsOrderingCycle/findFlashSaleWeekOutList")
    Response<List<FlashSaleWeekOut>> findFlashSaleWeekOutList(@RequestBody FlashSaleWeekIn flashSaleWeekIn);

    /**
     * 查询商品可订货时间段信息
     * @param goodsIn
     * @return
     */
    @ApiOperation(value = "根据门店代码和商品代码集合查询商品信息", notes = "根据门店代码和商品代码集合查询商品信息")
    @PostMapping("/gc/orderGoods/findGoodsInfoByStoreAndGoodsCodes")
    Response<List<OrderGoodsOut>> findGoodsInfoByStoreAndGoodsCodes(@RequestBody OrderGoodsIn goodsIn);

    /**
     * @Description: 有业务类型商品基本信息(运营端退货查询商品)
     * @Author: ZhangYao
     * @Date: 2023/8/5 14:22
     * @param orderGoodsIn:
     * @return: com.edc.plugins.common.response.Response<com.edc.erp.common.model.out.goods.OrderGoodsOut>
     **/
    @PostMapping("/gc/orderGoods/getStoreOrderGoodsForBackReturn")
    Response<OrderGoodsOut> getStoreOrderGoodsForBackReturn(@RequestBody OrderGoodsIn orderGoodsIn);

    @ApiOperation(value = "查找中科映射商品", notes = "查找中科映射商品", httpMethod = "GET")
    @GetMapping("/gc/zkGoods/getZkGoodsByGoodsCodeAndBizOrgCode")
    Response<ZkGoodsOut> getZkGoodsByGoodsCodeAndBizOrgCode(@RequestParam(value = "goodsCode") String goodsCode, @RequestParam(value = "bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "查找标准品", notes = "查找标准品", httpMethod = "POST")
    @PostMapping("/gc/standardGoodsInfo/findAllByGoodsCodeList")
    Response<List<StandardGoodsInfoOut>> findAllByGoodsCodeList(@RequestBody List<String> goodsCodeList);

    @ApiOperation(value = "订单业务查询可用商品", notes = "订单业务查询可用商品", httpMethod = "POST")
    @PostMapping("/gc/queryGoodsForOrd/findStoreGoodsInfos")
    Response<List<GoodsForOrdOut>> findGoodsInfos(@RequestBody OrdQueryGoodsIn ordQueryGoodsIn);

//    @ApiOperation(value = "根据codes和组织获取组织商品信息列表(翻译用)", notes = "获取组织商品基础信息", httpMethod = "GET")
//    @GetMapping("/gc/orgGoodsInfo/findTransInfos")
//    Response<OrgGoodsTransInfo> findTransInfos(@RequestBody FindForPurGoodsIn findForPurGoodsIn);


//    @PostMapping("/gc/orderGoods/findGoodsInfoForCheckUppLower")
//    Response<List<CheckUpperLowerGoodsOut>> findGoodsInfoForCheckUppLower(@RequestBody QueryCheckUpperLowerGoodsIn queryCheckUpperLowerGoodsIn);

    @PostMapping("/gc/queryGoodsForOrd/findForUpDownLimit")
    Response<List<OrderGoodsOut>> findForUpDownLimit(@RequestBody OrdQueryGoodsIn ordQueryGoodsIn);

    @PostMapping("/gc/orgGoodsInfo/findValidityPeriodInfos")
    Response<List<GoodsValidityPeriodInfo>> findValidityPeriodInfos(@RequestParam List<String> goodsCodes, @RequestParam String bizOrgCode);

    @ApiOperation(value = "查询指定组织商品的配送规格信息", notes = "查询指定组织商品的配送规格信息")
    @GetMapping("gc/orgGoodsInfo/getGoodsDisSpecOutByOrg")
    Response<GoodsDisSpecOut> getGoodsDisSpecOutByOrg(@RequestParam("goodsCode") String goodsCode, @RequestParam(required = false) String bizOrgCode);

    @ApiOperation(value = "查询指定组织商品集合的配送规格信息", notes = "查询指定组织商品集合的配送规格信息")
    @PostMapping("gc/orgGoodsInfo/findGoodsDisSpecOutByOrg")
    Response<List<GoodsDisSpecOut>> findGoodsDisSpecOutByOrg(@RequestBody OrdQueryGoodsIn ordQueryGoodsIn);

    /**
     * 查商品基本信息退货（无业务类型）
     * @param orderGoodsIn
     * @return
     */
    @PostMapping("gc/orderGoods/getStoreGoodsNoType")
    Response<OrderGoodsOut> getStoreGoodsNoType(@RequestBody OrderGoodsIn orderGoodsIn);

}
