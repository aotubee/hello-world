package com.edc.erp.presale.service.impl;

import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.erp.presale.mapper.OrdDisPresaleActivityGoodsMapper;
import com.edc.erp.presale.service.OrdDisPresaleActivityGoodsService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName OrdDisPresaleActivityGoodsServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 15:27
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleActivityGoodsServiceImpl extends BaseServiceImpl<OrdDisPresaleActivityGoods> implements OrdDisPresaleActivityGoodsService {

    private final OrdDisPresaleActivityGoodsMapper ordDisPresaleActivityGoodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSavePresaleActivityGoods(List<OrdDisPresaleActivityGoods> ordDisPresaleActivityGoodsList) {
        ordDisPresaleActivityGoodsMapper.batchSavePresaleActivityGoods(ordDisPresaleActivityGoodsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByActivityId(Long activityId) {
        ordDisPresaleActivityGoodsMapper.deleteByActivityId(activityId);
    }

    @Override
    public List<OrdDisPresaleActivityGoods> findGoodsListByPresaleActivityId(Long presaleActivityId) {
        OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = new OrdDisPresaleActivityGoods();
        ordDisPresaleActivityGoods.setPresaleActivityId(presaleActivityId);
        return ordDisPresaleActivityGoodsMapper.select(ordDisPresaleActivityGoods);
    }

    @Override
    public OrdDisPresaleActivityGoods getOneByIdAndActivityId(Long id, Long presaleActivityId) {
        OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = new OrdDisPresaleActivityGoods();
        ordDisPresaleActivityGoods.setPresaleActivityId(presaleActivityId);
        ordDisPresaleActivityGoods.setId(id);
        return ordDisPresaleActivityGoodsMapper.selectOne(ordDisPresaleActivityGoods);
    }

    @Override
    public List<OrdDisPresaleActivityGoods> findPresaleActivityGoodsByActivityIdAndGoodsCode(Long activityId, String goodsCode) {
        OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = new OrdDisPresaleActivityGoods();
        ordDisPresaleActivityGoods.setPresaleActivityId(activityId);
        ordDisPresaleActivityGoods.setGoodsCode(goodsCode);
        return ordDisPresaleActivityGoodsMapper.select(ordDisPresaleActivityGoods);
    }


    @Override
    public OrdDisPresaleActivityGoods getOneByGoodsCodeAndActivityId(String goodsCode, Long presaleActivityId, Integer isGift) {
        OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = new OrdDisPresaleActivityGoods();
        ordDisPresaleActivityGoods.setPresaleActivityId(presaleActivityId);
        ordDisPresaleActivityGoods.setGoodsCode(goodsCode);
        ordDisPresaleActivityGoods.setIsGift(isGift);
        return ordDisPresaleActivityGoodsMapper.selectOne(ordDisPresaleActivityGoods);
    }
}
