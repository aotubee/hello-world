package com.edc.erp.disrequestorder.service;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderDtlPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderDetailOut;
import com.edc.erp.disrequestorder.model.out.DisRequestSummarizingOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.List;



/**
 * 集货单明细表(OrdDisDelivRequestDetail)表服务接口
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
public interface OrdDisDelivRequestDetailService extends BaseService<OrdDisDelivRequestDetail> {
    /**
     * 根据集货单ID查询集货单商品数量总和
     * @param requestOrderId
     * @return
     */
    BigDecimal getSkuNumberById(Long requestOrderId);

    /**
     * 根据集货单ID查询集货单商品品项数
     * @param requestOrderId
     * @return
     */
    Integer getGoodsItemNumber(Long requestOrderId);

    /**
     * 根据集货单主键查询集货单明细
     * @param dtlPageIn
     * @return
     */
    Page<BackRequestOrderDetailOut> findRequestOrderDetailList(BackQueryRequestOrderDtlPageIn dtlPageIn);

    /**
     * 导出集货单明细
     * @param dtlPageIn
     * @return
     */
    String exportDetailList(BackQueryRequestOrderDtlPageIn dtlPageIn);

    /**
     * 批量新增集货单详情表
     *
     * @param requestOrderDetailList
     */
    void batchSave(List<OrdDisDelivRequestDetail> requestOrderDetailList);

    /**
     * 根据集货单id查询集货单明细信息
     *
     * @param requestOrderId
     * @return
     */
    List<OrdDisDelivRequestDetail> findByRequestOrderIdAndBizOrgCode(Long requestOrderId);

}
