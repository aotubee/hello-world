package com.edc.erp.presale.service;

import com.edc.erp.presale.model.in.PresaleGoodsFlowPageIn;
import com.edc.erp.presale.model.out.PresaleGoodsFlowPageOut;
import com.edc.plugins.common.model.page.Page;

public interface OrdDisPresaleGoodsFlowService {
    /**
     * 预售商品流水分页查询列表
     * @param presaleGoodsFlowPageIn
     * @return
     */
    Page<PresaleGoodsFlowPageOut> findPresaleGoodsFlowByPage(PresaleGoodsFlowPageIn presaleGoodsFlowPageIn);

    /**
     * 导出预售商品流水列表
     * @param presaleGoodsFlowPageIn
     * @return
     */
    String exportPresaleGoodsFlowList(PresaleGoodsFlowPageIn presaleGoodsFlowPageIn);
}
