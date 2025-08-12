package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.SaleGoodsInfoService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * 查询允许批发出货业务的商品信息 RPC调用实现类
 * @author lx
 * @since 2022-10-28 10:25:58
 */
@Service
@Slf4j
public class SaleGoodsInfoServiceImpl implements SaleGoodsInfoService {
    @Resource
    private SaleGoodsInfoClient saleGoodsInfoClient;

    /**
     * 查询允许批发出货业务的商品信息
     * @param querySaleGoodsInfoIn 查询允许批发出货业务的商品信息 入参类
     * @return
     */
    @Override
    public SaleGoodsInfoOut getGoodsInfo(QuerySaleGoodsInfoIn querySaleGoodsInfoIn) {
        //查询允许批发出货业务的商品信息
        Response<SaleGoodsInfoOut> goodsInfo = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if(!goodsInfo.isSuccess() || Objects.isNull(goodsInfo.getData())){
            log.error(goodsInfo.getMessage());
            return null;
        }
        return goodsInfo.getData();
    }
}
