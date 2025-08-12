package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 查询允许批发出货业务的商品信息 远程调用
 * @author lx
 * @since 2022-10-28 10:06:21
 */
@FeignClient(name ="mdm",contextId ="SaleGoodsInfoClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface SaleGoodsInfoClient {

    /**
     * 查询允许批发出货业务的商品信息
     * @param querySaleGoodsInfoIn 查询允许批发出货业务的商品信息 入参类
     * @return
     */
    @GetMapping("/gc/orderGoods/getGoodsInfo")
    Response<SaleGoodsInfoOut> getGoodsInfo(@SpringQueryMap QuerySaleGoodsInfoIn querySaleGoodsInfoIn);

    /**
     * 批量查询允许批发出货业务的商品信息
     * @param querySaleGoodsInfoIn 查询允许批发出货业务的商品信息 入参类
     * @return
     */
    @PostMapping("/gc/orderGoods/findGoodsInfo")
    Response<List<SaleGoodsInfoOut>> findGoodsInfo(@RequestBody QuerySaleGoodsInfoIn querySaleGoodsInfoIn);


    /**
     * @Description: 根据商品代码获取标准商品规格列表
     * @Author: ZhangYao
     * @Date: 2024/10/26 14:12
     * @param goodsCodes:
     * @return: com.edc.plugins.common.response.Response<Map<String,List<StandardSpecOut>>>
     **/
    @PostMapping("/gc/standardSpec/findByGoodsCodes")
    Response<Map<String, List<StandardSpecOut>>> findByGoodsCodes(@RequestBody List<String> goodsCodes);
}
