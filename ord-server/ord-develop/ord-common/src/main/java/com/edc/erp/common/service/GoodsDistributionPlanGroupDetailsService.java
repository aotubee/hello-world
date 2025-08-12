package com.edc.erp.common.service;


import com.edc.erp.common.model.out.goodsdistributionplan.GoodsDistributionPlanGroupDetailsVendorOut;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description: 商品配送方案明细
 * @Author: fxw
 * @Date: 2022/11/23
 */
public interface GoodsDistributionPlanGroupDetailsService {


   /**
    * 查配送方案信息
    * @param goodsCode
    * @param bizOrgCode
    * @param alcSchemeCode
    * @return
    */
   GoodsDistributionPlanGroupDetailsVendorOut getPlanCodeByGoodsCode(@RequestParam String goodsCode, @RequestParam String bizOrgCode, @RequestParam String alcSchemeCode);
}
