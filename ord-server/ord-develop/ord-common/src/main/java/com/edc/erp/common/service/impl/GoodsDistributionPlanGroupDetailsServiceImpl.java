package com.edc.erp.common.service.impl;


import com.edc.erp.common.model.out.goodsdistributionplan.GoodsDistributionPlanGroupDetailsVendorOut;
import com.edc.erp.common.rpc.GoodsDistributionPlanGroupDetailsClient;
import com.edc.erp.common.service.GoodsDistributionPlanGroupDetailsService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @return: 商品配送方案明细实现
* @Author: fxw
* @Date: 2022/11/23
*/
@Service
@Slf4j
public class GoodsDistributionPlanGroupDetailsServiceImpl implements GoodsDistributionPlanGroupDetailsService {

    @Autowired
    private GoodsDistributionPlanGroupDetailsClient goodsDistributionPlanGroupDetailsClient;
    /**
     * 查配送放案信息
     *
     * @param goodsCode
     * @param bizOrgCode
     * @param alcSchemeCode
     * @return
     */
    @Override
    public GoodsDistributionPlanGroupDetailsVendorOut getPlanCodeByGoodsCode(String goodsCode, String bizOrgCode, String alcSchemeCode) {

        Response<GoodsDistributionPlanGroupDetailsVendorOut> response = goodsDistributionPlanGroupDetailsClient.getPlanCodeByGoodsCode(goodsCode, bizOrgCode, alcSchemeCode);
        if(!response.isSuccess() || Objects.isNull(response.getData())){
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
