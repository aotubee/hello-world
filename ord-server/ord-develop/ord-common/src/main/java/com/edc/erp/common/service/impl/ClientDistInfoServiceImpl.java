package com.edc.erp.common.service.impl;

import com.edc.erp.common.mapper.StoreCenterMapper;
import com.edc.erp.common.model.entity.ClientInfo;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.customer.QueryClientInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.rpc.ClientDistInfoClient;
import com.edc.erp.common.service.ClientDistInfoService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author lx
 * @since 2022-11-02 11:07:18
 */
@Service
@Slf4j
public class ClientDistInfoServiceImpl implements ClientDistInfoService {

    @Resource
    private ClientDistInfoClient clientDistInfoClient;

    @Autowired
    private StoreCenterMapper storeCenterMapper;

    @Override
    public List<ClientDistInfoOut> findClientDistInfo(QueryClientDistInfoIn queryClientDistInfoIn) {
        Response<List<ClientDistInfoOut>> clientDistInfo = clientDistInfoClient.findClientDistInfo(queryClientDistInfoIn);
        if(!clientDistInfo.isSuccess() || Objects.isNull(clientDistInfo.getData())){
            log.error(clientDistInfo.getMessage());
            return null;
        }
        return clientDistInfo.getData();
    }

    @Override
    public boolean getIsIndependentAccountingByCode(String clientCode, String bizOrgCode) {
        Integer isIndependentAccounting = storeCenterMapper.getIsIndependentAccountingByCode(clientCode, bizOrgCode);
        return NumberUtils.INTEGER_ONE.equals(isIndependentAccounting);
    }

    @Override
    public boolean getIsCanEditPrice(String clientCode, String bizOrgCode) {
        Integer isCanEditPrice = storeCenterMapper.getIsCanEditPrice(clientCode, bizOrgCode);
        return NumberUtils.INTEGER_ONE.equals(isCanEditPrice);
    }

    @Override
    public ClientInfo getClientInfoByCode(String clientCode, String bizOrgCode) {
        return storeCenterMapper.getClientInfoByCode(clientCode, bizOrgCode);
    }

    @Override
    public ClientDistInfoOut getOneByParameter(QueryClientInfoIn queryClientInfoIn) {
        Response<ClientDistInfoOut> response = clientDistInfoClient.getOneByParameter(queryClientInfoIn);
        if(!response.isSuccess() || Objects.isNull(response.getData())){
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
