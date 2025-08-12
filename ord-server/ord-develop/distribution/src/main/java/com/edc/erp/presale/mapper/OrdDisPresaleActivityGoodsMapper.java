package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleActivityGoodsMapper extends BaseMapper<OrdDisPresaleActivityGoods> {

    void batchSavePresaleActivityGoods(@Param("ordDisPresaleActivityGoodsList") List<OrdDisPresaleActivityGoods> ordDisPresaleActivityGoodsList);

    void deleteByActivityId(@Param("activityId") Long activityId);
}
