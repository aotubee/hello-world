package com.edc.erp.common.service;

import com.edc.plugins.common.response.Response;
import com.edc.sdk.dts.model.order.vo.*;

/**
 * @author yaojinpeng
 * @since 2022/11/1 12:10
 */
public interface UnificationOrderService {

    /**
     * 订货单回调
     *
     * @param unificationBillVO
     * @return
     */
    Response<String> unificationOrderCallBack(UnificationBillVO unificationBillVO);

    /**
     * 退货单回调
     *
     * @param unificationReBillVO
     * @return
     */
    Response<String> unificationReOrderCallBack(UnificationReBillVO unificationReBillVO);

    /**
     * 差异单回调
     *
     * @param differenceBillVO
     * @return
     */
    Response<String> differenceOrderCallBack(DifferenceBillVO differenceBillVO);

    /**
     * 批发退货单DTS回传
     * @param wholesaleReBillVO
     * @return
     */
    Response<String> wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO);

    /**
     * 批发出货单DTS回传
     * @param wholesaleBillVO 批发单返回入参类
     * @return
     */
    Response<String> wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO);
}
