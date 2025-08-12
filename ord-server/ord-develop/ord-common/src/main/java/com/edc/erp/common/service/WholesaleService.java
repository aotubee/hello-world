package com.edc.erp.common.service;

import com.edc.erp.common.model.entity.OrgGoodsInfo;
import com.edc.erp.common.model.in.returns.ClientWholesaleConfigIn;
import com.edc.erp.common.model.out.returns.ClientWholesaleConfigOut;
import com.edc.plugins.common.response.Response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批发业务远程调用Service
 *
 * @author wanglidong
 * @Since 2022/10/26 20:01
 */
public interface WholesaleService {


    /**
     * 查询当前组织下的批发客户价格组配置信息
     *
     * @param clientWholesaleConfigIn
     * @return
     */
    public List<ClientWholesaleConfigOut> findWholesalePriceGroup(ClientWholesaleConfigIn clientWholesaleConfigIn);

    /**
     * 根据客户代码和组织代码查询客户状态
     *
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    public Response selectClientStatus(String clientCode, String bizOrgCode);

    /**
     * 根据商品代码查询组织商品详情
     *
     * @param goodsCode
     * @return
     */
    public OrgGoodsInfo getOrgGoodsInfo(String goodsCode);

    /**
     * 根据条件获取批发价格
     * @param bizOrgCode
     * @param priceGroupCode
     * @param clientCode
     * @param goodsCode
     * @return
     */
    public BigDecimal getPrice(String bizOrgCode, String priceGroupCode, String clientCode, String goodsCode);

    /**
     * 通过客户代码和组织代码查询客户ID，无返回0
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    Integer getClientIdByCode(String clientCode, String bizOrgCode);
}
