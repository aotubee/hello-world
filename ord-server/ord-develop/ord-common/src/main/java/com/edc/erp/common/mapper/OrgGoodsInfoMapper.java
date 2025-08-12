package com.edc.erp.common.mapper;

import com.edc.erp.common.model.entity.OrgGoodsInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
/**
 * @return: 组织商品信息mapper
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Mapper
public interface OrgGoodsInfoMapper {

    /**
     * 根据商品代码查询组织商品信息
     * @param goodsCode
     * @return
     */
    OrgGoodsInfo selectByGoodsCode(@Param("goodsCode") String goodsCode);

    /**
     * 根据条件获取批发价格
     * @param bizOrgCode
     * @param priceGroupCode
     * @param clientCode
     * @param goodsCode
     * @return
     */
    BigDecimal getPrice(@Param("bizOrgCode") String bizOrgCode, @Param("priceGroupCode") String priceGroupCode, @Param("clientCode") String clientCode, @Param("goodsCode") String goodsCode);

    Integer getGoodsLogisticsByGoodsCode(@Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件获取零售价格
     * @param bizOrgCode
     * @param storeCode
     * @param goodsCode
     * @return
     */
    BigDecimal getSellPrice(@Param("bizOrgCode") String bizOrgCode, @Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode);
}