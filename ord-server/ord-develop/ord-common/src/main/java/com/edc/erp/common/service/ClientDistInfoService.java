package com.edc.erp.common.service;

import com.edc.erp.common.model.entity.ClientInfo;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.customer.QueryClientInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;

import java.util.List;

/**
 * @author lx
 * @since 2022-11-02 11:06:18
 */
public interface ClientDistInfoService {

    /**
     * 根据客户代码或配送信息ID查询客户信息及配送信息
     * @param queryClientDistInfoIn 询客户信息 入参类
     * @return
     */
    List<ClientDistInfoOut> findClientDistInfo(QueryClientDistInfoIn queryClientDistInfoIn);

    /**
     * 是否独立核算
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    boolean getIsIndependentAccountingByCode(String clientCode, String bizOrgCode);

    boolean getIsCanEditPrice(String clientCode, String bizOrgCode);

    ClientInfo getClientInfoByCode(String clientCode, String bizOrgCode);

    ClientDistInfoOut getOneByParameter(QueryClientInfoIn queryClientInfoIn);
}
