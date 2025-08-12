package com.edc.erp.common.service;

import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndFreezeIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndPayIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.plugins.common.response.Response;

import java.util.concurrent.Future;

public interface AsyncFundService {


    Future<Response> asyncUnFrozenAndFrozen(UnFrozenAndFreezeIn unFrozenAndFreezeIn);

    Response asyncUnFrozen(UnFrozenIn unFrozenIn);

    Response asyncFrozen(StoreFrozenIn storeFrozenIn);

    Response asyncUnFrozenAndSettlement(UnFrozenAndPayIn unFrozenAndPayIn);
}
