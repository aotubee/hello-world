package com.edc.erp.common.async.handel;

import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.service.AsyncTaskService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 订单异步处理
 *
 * @author weichao
 */
@Slf4j
@Service
public class SyncOrdDisOrderHandle {

    @Autowired
    private AsyncTaskService asyncTaskService;


    /**
     * 调资管中心清算
     */
    public Response syncOrderToFund(RechargeLiquidationIn rechargeLiquidationIn) {
        Future<Response> responseFuture = asyncTaskService.syncOrderToFund(rechargeLiquidationIn);
        Response response;
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
        return response;
    }

    public Response syncToFrozen(StoreFrozenIn storeFrozenIn) {
        Future<Response> responseFuture = asyncTaskService.syncFrozen(storeFrozenIn);
        Response response = null;
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response syncToUnFrozen(UnFrozenIn unFrozenIn) {
        Future<Response> responseFuture = asyncTaskService.syncUnFrozen(unFrozenIn);
        Response response = null;
        try {
            response = responseFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
