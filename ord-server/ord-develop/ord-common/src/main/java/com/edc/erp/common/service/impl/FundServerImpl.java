package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.in.fund.*;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.erp.common.rpc.FundClient;
import com.edc.erp.common.service.FundServer;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * 资管接口实现类
 *
 * @author weichao
 */
@Service
@Slf4j
public class FundServerImpl implements FundServer {

    @Autowired
    private FundClient fundClient;

    @Override
    public ForeignAccountFundOut getAvailableAmount(ForeignAccountFundIn foreignAccountFundIn) {
        Response<ForeignAccountFundOut> response = fundClient.getAvailableAmount(foreignAccountFundIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("门店{}查询账户余额异常{}", foreignAccountFundIn.getPrincipalCode(), response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public Response settlement(RechargeLiquidationIn rechargeLiquidationIn) {
        Response response = null;
        try {
            response = fundClient.settlement(rechargeLiquidationIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return response;
    }


    @Override
    @Async
    public Future<Response> asyncUnFrozen(UnFrozenIn unFrozenIn) {
        Response response;
        try {
            response = fundClient.unFrozen(unFrozenIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return new AsyncResult<>(response);
    }

    @Override
    @Async
    public Future<Response> asyncFrozen(StoreFrozenIn storeFrozenIn) {
        Response response;
        try {
            response = fundClient.frozen(storeFrozenIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return new AsyncResult<>(response);
    }

    @Override
    @Async
    public Future<Response> asyncUnFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn) {
        Response response;
        try {
            response = fundClient.unFrozenAndSettlement(unFrozenAndPayIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return new AsyncResult<>(response);
    }

    @Override
    public Response unFrozenAndFrozen(UnFrozenAndFreezeIn unFrozenAndFreezeIn) {
        Response response;
        try {
            response = fundClient.unFrozenAndFrozen(unFrozenAndFreezeIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return response;
    }

    @Override
    public Response unFrozen(UnFrozenIn unFrozenIn) {
        Response response;
        try {
            response = fundClient.unFrozen(unFrozenIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return response;
    }

    @Override
    public Response frozen(StoreFrozenIn storeFrozenIn) {
        Response response;
        try {
            response = fundClient.frozen(storeFrozenIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return response;
    }

    @Override
    public Response unFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn) {
        Response response;
        try {
            response = fundClient.unFrozenAndSettlement(unFrozenAndPayIn);
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.error(e.getMessage());
        }
        if (Objects.nonNull(response) && !response.isSuccess()) {
            log.error(response.getMessage());
        }
        return response;
    }
}
