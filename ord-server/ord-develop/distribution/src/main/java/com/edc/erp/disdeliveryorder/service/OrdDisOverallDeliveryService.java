package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.model.in.DisOverallDeliveryAsyncImportIn;
import com.edc.plugins.common.response.Response;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @ClassName OrdDisOverallDeliveryService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/22 15:58
 **/
public interface OrdDisOverallDeliveryService {

    Response<String> asyncImportOverallDelivery(DisOverallDeliveryAsyncImportIn disOverallDeliveryAsyncImportIn, String loginUsername, String loginBizOrgCode);

}
