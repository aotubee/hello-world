package com.edc.erp.common.service;

import com.edc.erp.common.model.in.fund.*;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.plugins.common.response.Response;

import java.util.concurrent.Future;

import java.util.List;


/**
 * 资管接口
 *
 * @author weichao
 */
public interface FundServer {
    /**
     * 校验账户资金
     * @param foreignAccountFundIn
     * @return
     */
    ForeignAccountFundOut getAvailableAmount(ForeignAccountFundIn foreignAccountFundIn);

    /**
     * 账户资金变动
     * @param rechargeLiquidationIn
     * @return
     */
    Response settlement(RechargeLiquidationIn rechargeLiquidationIn);

    /**
     * @Description: 批量解冻原单，批量冻结新单
     * @Author: ZhangYao
     * @Date: 2023/7/20 8:39
     * @param unFrozenAndFreezeIn:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response unFrozenAndFrozen(UnFrozenAndFreezeIn unFrozenAndFreezeIn);

    /**
     * @Description: 批量解冻原单
     * @Author: ZhangYao
     * @Date: 2023/7/20 11:05
     * @param unFrozenIn:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response unFrozen(UnFrozenIn unFrozenIn);

    /**
     * @Description: 冻结原单
     * @Author: ZhangYao
     * @Date: 2023/7/21 11:01
     * @param storeFrozenIn:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response frozen(StoreFrozenIn storeFrozenIn);

    Response unFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn);

    Future<Response> asyncUnFrozen(UnFrozenIn unFrozenIn);


    Future<Response> asyncFrozen(StoreFrozenIn storeFrozenIn);



    Future<Response> asyncUnFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn);
}
