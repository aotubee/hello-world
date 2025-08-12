package com.edc.erp.common.service;

import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;

/**
 * 查询允许批发出货业务的商品信息 RPC接口类
 * @author lx
 * @since 2022-10-28 10:24:53
 */
public interface SaleGoodsInfoService {

    /**
     * 查询允许批发出货业务的商品信息
     * @param querySaleGoodsInfoIn 查询允许批发出货业务的商品信息 入参类
     * @return
     */
    SaleGoodsInfoOut getGoodsInfo(QuerySaleGoodsInfoIn querySaleGoodsInfoIn);
}
