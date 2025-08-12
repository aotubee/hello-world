package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleGoodsFlow;
import com.edc.erp.presale.model.in.PresaleGoodsFlowPageIn;
import com.edc.erp.presale.model.out.PresaleGoodsFlowPageOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleGoodsFlowMapper extends BaseMapper<OrdDisPresaleGoodsFlow> {
    /**
     * 分页查询预售商品流水
     * @param presaleGoodsFlowPageIn
     * @return
     */
    List<PresaleGoodsFlowPageOut> findPresaleGoodsFlowByPage(PresaleGoodsFlowPageIn presaleGoodsFlowPageIn);
}
