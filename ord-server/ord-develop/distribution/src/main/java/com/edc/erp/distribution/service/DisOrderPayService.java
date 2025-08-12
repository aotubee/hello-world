package com.edc.erp.distribution.service;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.plugins.common.response.Response;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DisOrderPayService {

    boolean updateDisOrderPaidSuccess(Long id, String operator);

    Response<String> disOrderPay(List<Long> orderIdList, AppUserOut appUserOut);

    @Transactional(rollbackFor = Exception.class)
    void handleStoreDisOrderPay(Long orderId, String loginUsername);
}
