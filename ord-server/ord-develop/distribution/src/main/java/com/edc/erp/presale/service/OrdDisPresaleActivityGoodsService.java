package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrdDisPresaleActivityGoodsService extends BaseService<OrdDisPresaleActivityGoods> {

    @Transactional(rollbackFor = Exception.class)
    void batchSavePresaleActivityGoods(List<OrdDisPresaleActivityGoods> ordDisPresaleActivityGoodsList);

    void deleteByActivityId(Long activityId);

    List<OrdDisPresaleActivityGoods> findGoodsListByPresaleActivityId(Long presaleActivityId);

    OrdDisPresaleActivityGoods getOneByIdAndActivityId(Long id, Long presaleActivityId);


    List<OrdDisPresaleActivityGoods> findPresaleActivityGoodsByActivityIdAndGoodsCode(Long activityId, String goodsCode);

    OrdDisPresaleActivityGoods getOneByGoodsCodeAndActivityId(String goodsCode, Long presaleActivityId, Integer isGift);
}
