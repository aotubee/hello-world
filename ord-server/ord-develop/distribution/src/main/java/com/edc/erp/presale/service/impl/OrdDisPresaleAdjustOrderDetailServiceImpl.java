package com.edc.erp.presale.service.impl;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrderDetail;
import com.edc.erp.presale.mapper.OrdDisPresaleAdjustOrderDetailMapper;
import com.edc.erp.presale.model.in.PresaleAdjustOrderSaveIn;
import com.edc.erp.presale.service.OrdDisPresaleAdjustOrderDetailService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDisPresaleAdjustOrderDetailServiceImpl
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:37
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleAdjustOrderDetailServiceImpl extends BaseServiceImpl<OrdDisPresaleAdjustOrderDetail> implements OrdDisPresaleAdjustOrderDetailService {
    private final OrdDisPresaleAdjustOrderDetailMapper ordDisPresaleAdjustOrderDetailMapper;

    @Override
    public void deleteByAdjustOrderId(Long adjustOrderId) {
        ordDisPresaleAdjustOrderDetailMapper.delete(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(adjustOrderId).build());
    }

    @Override
    public void savePresaleAdjustOrderDetail(Long adjustOrderId, PresaleAdjustOrderSaveIn orderSaveIn) {
        List<OrdDisPresaleAdjustOrderDetail> list = orderSaveIn.getDetails().stream().map(item -> OrdDisPresaleAdjustOrderDetail.builder()
                .adjustOrderId(adjustOrderId)
                .goodsCode(item.getGoodsCode())
                .goodsName(item.getGoodsName())
                .barCode(item.getBarCode())
                .adjustQty(item.getAdjustQty())
                .beforeQty(item.getBeforeQty())
                .goodsType(item.getGoodsType())
                .packageUnit(item.getPackageUnit())
                .packageSpecification(item.getPackageSpecification())
                .packageSpecificationNum(item.getPackageSpecificationNum())
                .packageUnit(item.getPackageUnit())
                .presaleActivityNo(item.getPresaleActivityNo())
                .creator(UserUtil.getUserName())
                .createTime(LocalDateTime.now())
                .isDelete(ModelConst.DELETE.NO)
                .build()
        ).collect(Collectors.toList());
        ordDisPresaleAdjustOrderDetailMapper.batchInsert(list);
    }
}
