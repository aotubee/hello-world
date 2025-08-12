package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.GoodsCombination;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 商品组合表(GoodsCombination)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-19 15:08:50
 */
@Repository
public interface GoodsCombinationMapper extends BaseMapper<GoodsCombination> {

    /**
     * 根据组织和订单类型id查找商品组合集合
     *
     * @param bizOrgCode
     * @param orderTypeConfigIdList
     * @return
     */
    List<GoodsCombination> findByOrgCodeAndOrderTypeConfigIdList(@Param("bizOrgCode") String bizOrgCode,
                                                                 @Param("orderTypeConfigIdList") List<Integer> orderTypeConfigIdList);
}
