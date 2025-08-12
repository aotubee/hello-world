package com.edc.erp.presale.service;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.model.in.QueryStorePresaleOrderForAppIn;
import com.edc.erp.model.in.QueryStorePresaleOrderIn;
import com.edc.erp.model.out.DisStorePresaleOrderGetForAppOut;
import com.edc.erp.model.out.DisStorePresaleOrderInfoOut;
import com.edc.erp.model.out.DisStorePresaleOrderPageForAppOut;
import com.edc.erp.presale.entity.OrdDisPresaleOrder;
import com.edc.erp.presale.model.in.SubmitPresaleOrderIn;
import com.edc.erp.presale.model.out.CreatePresaleOrderDataOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

public interface OrdDisPresaleOrderService {
    CreatePresaleOrderDataOut handleDisPresaleOrderData(SubmitPresaleOrderIn submitPresaleOrderIn);

    @Transactional(rollbackFor = Exception.class)
    Response<Long> saveDisPresaleOrder(CreatePresaleOrderDataOut createPresaleOrderDataOut);

    Response<String> payPresaleOrder(Long id, AppUserOut appUserOut, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    void updatePresaleOrderAfterPaidSuccess(Long id, String loginUsername);

    Page<DisStorePresaleOrderPageForAppOut> findStorePresaleOrderForApp(QueryStorePresaleOrderForAppIn queryStorePresaleOrderForAppIn);

    DisStorePresaleOrderGetForAppOut getStorePresaleOrderInfoForApp(Long id);

    Page<DisStorePresaleOrderInfoOut> findStorePresaleOrderByPage(QueryStorePresaleOrderIn queryStorePresaleOrderIn);

    DisStorePresaleOrderInfoOut getStorePresaleOrderInfo(Long id);

    String exportPresaleOrderList(QueryStorePresaleOrderIn queryStorePresaleOrderIn);

    String exportPresaleOrderDetailList(Long id);

    OrdDisPresaleOrder getOrdDisPresaleOrder(Long id);

    boolean cancelStorePresaleOrder(@NotNull Long id);

    Response<String> checkIsCanRefund(OrdDisPresaleOrder ordDisPresaleOrder);

    Response<String> refundPresaleOrder(OrdDisPresaleOrder ordDisPresaleOrder, String loginUsername);

    OrdDisPresaleOrder getOneByIdAndBizOrgCode(Long id, String bizOrgCode);

}
