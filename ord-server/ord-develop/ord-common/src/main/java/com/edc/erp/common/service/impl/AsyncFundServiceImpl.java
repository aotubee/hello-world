package com.edc.erp.common.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndFreezeIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndPayIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.service.AsyncFundService;
import com.edc.erp.common.service.FundServer;
import com.edc.plugins.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @ClassName AsyncFundServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/10/5 11:53
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncFundServiceImpl implements AsyncFundService {

    private final FundServer fundServer;

    @Override
    @Async
    public Future<Response> asyncUnFrozenAndFrozen(UnFrozenAndFreezeIn unFrozenAndFreezeIn) {
        Response response = fundServer.unFrozenAndFrozen(unFrozenAndFreezeIn);
        return new AsyncResult<>(response);
    }

    @Override
    public Response asyncUnFrozen(UnFrozenIn unFrozenIn) {
        Response response = null;
        Future<Response> responseFuture = fundServer.asyncUnFrozen(unFrozenIn);
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            log.error("单号{}异步调用释放资金异常", JSONArray.toJSON(unFrozenIn.getUnFrozenBusinessNos()), e);
        }
        return response;
    }

    @Override
    public Response asyncFrozen(StoreFrozenIn storeFrozenIn) {
        Response response = null;
        Future<Response> responseFuture = fundServer.asyncFrozen(storeFrozenIn);
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            log.error("单号{}异步调用冻结资金异常", JSONArray.toJSON(storeFrozenIn.getFrozenOrders()), e);
        }
        return response;
    }

    @Override
    public Response asyncUnFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn) {
        Response response = null;
        Future<Response> responseFuture = fundServer.asyncUnFrozenAndSettlement(unFrozenAndPayIn);
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            log.error("单号{}异步调用释放+实扣资金异常", JSONArray.toJSON(unFrozenAndPayIn.getUnFrozenBusinessNo()), e);
        }
        return response;
    }
}
