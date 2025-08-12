package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrder;
import com.edc.erp.presale.model.in.PresaleAdjustOrderPageIn;
import com.edc.erp.presale.model.out.PresaleAdjustOrderPageOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleAdjustOrderMapper extends BaseMapper<OrdDisPresaleAdjustOrder> {
    /**
     * 分页查询预售调整单
     * @param presaleAdjustOrderPageIn
     * @return
     */
    List<PresaleAdjustOrderPageOut> findPresaleAdjustOrderByPage(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn);
}
