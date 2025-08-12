package com.edc.erp.common.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.edc.erp.common.mapper.OrgGoodsInfoMapper;
import com.edc.erp.common.model.entity.OrgGoodsInfo;
import com.edc.erp.common.model.in.returns.ClientWholesaleConfigIn;
import com.edc.erp.common.model.out.returns.ClientWholesaleConfigOut;
import com.edc.erp.common.rpc.WholesaleClient;
import com.edc.erp.common.service.WholesaleService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 批发业务远程调用Service实现类
 *
 * @author wanglidong
 * @Since 2022/10/26 20:01
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WholesaleServiceImpl implements WholesaleService {

    @Autowired
    private WholesaleClient wholesaleClient;
    @Autowired
    private OrgGoodsInfoMapper orgGoodsInfoMapper;

    /**
     * 查询当前组织下的批发客户价格组配置信息
     *
     * @param clientWholesaleConfigIn
     * @return
     */
    @Override
    public List<ClientWholesaleConfigOut> findWholesalePriceGroup(ClientWholesaleConfigIn clientWholesaleConfigIn) {
        Response<Page<ClientWholesaleConfigOut>> pageResponse = wholesaleClient.findWholesalePriceGroup(clientWholesaleConfigIn);
        if (!pageResponse.isSuccess() || ObjectUtil.isNull(pageResponse.getData().getList())) {
            log.error(pageResponse.getMessage());
            return Collections.emptyList();
        }
        return pageResponse.getData().getList();
    }

    /**
     * 根据客户代码和组织代码查询客户状态
     *
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public Response selectClientStatus(String clientCode, String bizOrgCode) {
        Response response = wholesaleClient.selectClientStatus(clientCode, StrUtil.isBlank(bizOrgCode) ? UserUtil.getBizOrgCode() : bizOrgCode);
        return response;
    }

    /**
     * 根据商品代码查询组织商品详情
     *
     * @param goodsCode
     * @return
     */
    @Override
    public OrgGoodsInfo getOrgGoodsInfo(String goodsCode) {
        return orgGoodsInfoMapper.selectByGoodsCode(goodsCode);
    }

    /**
     * 根据条件获取批发价格
     *
     * @param bizOrgCode
     * @param priceGroupCode
     * @param clientCode
     * @param goodsCode
     * @return
     */
    @Override
    public BigDecimal getPrice(String bizOrgCode, String priceGroupCode, String clientCode, String goodsCode) {
        return orgGoodsInfoMapper.getPrice(bizOrgCode,priceGroupCode,clientCode,goodsCode);
    }

    @Override
    public Integer getClientIdByCode(String clientCode, String bizOrgCode) {
        ClientWholesaleConfigIn clientWholesaleConfigIn = new ClientWholesaleConfigIn();
        clientWholesaleConfigIn.setClientCode(clientCode);
        clientWholesaleConfigIn.setBizOrgCode(bizOrgCode);
        List<ClientWholesaleConfigOut> wholesalePriceGroupList = findWholesalePriceGroup(clientWholesaleConfigIn);
        Integer clientId = 0;
        lo:
        for (ClientWholesaleConfigOut clientWholesaleConfigOut : wholesalePriceGroupList) {
            if (clientWholesaleConfigOut.getPriceGroupCode().equals(clientCode)) {
                clientId = clientWholesaleConfigOut.getClientId();
                break lo;
            }
        }
        return clientId;
    }
}
