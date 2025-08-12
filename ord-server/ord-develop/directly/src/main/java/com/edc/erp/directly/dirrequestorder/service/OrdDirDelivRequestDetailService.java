package com.edc.erp.directly.dirrequestorder.service;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderDtlPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderDetailOut;
import com.edc.erp.directly.dirrequestorder.model.out.RequestSummarizingOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.List;



/**
 * 要货单明细表(OrdDirDelivRequestDetail)表服务接口
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
public interface OrdDirDelivRequestDetailService extends BaseService<OrdDirDelivRequestDetail> {
    /**
     * 根据要货单ID查询要货单商品数量总和
     * @param requestOrderId
     * @return
     */
    BigDecimal getSkuNumberById(Long requestOrderId);

    /**
     * 根据要货单ID查询要货单商品品项数
     * @param requestOrderId
     * @return
     */
    Integer getGoodsItemNumber(Long requestOrderId);

    /**
     * 查询要货单明细
     * @param dtlPageIn
     * @return
     */
    Page<DirRequestOrderDetailOut> findRequestOrderDetailList(DirRequestOrderDtlPageIn dtlPageIn);

    /**
     * 导出要货单明细
     * @param dtlPageIn
     * @return
     */
    String exportDetailList(DirRequestOrderDtlPageIn dtlPageIn);

    /**
     * 根据要货单id查询要货单明细
     * @param requestOrderId
     * @return
     */
    List<OrdDirDelivRequestDetail> findByRequestOrderIdAndBizOrgCode(Long requestOrderId);

    /**
     * 批量新增要货单详情表
     *
     * @param requestOrderDetailList
     */
    void batchSave(List<OrdDirDelivRequestDetail> requestOrderDetailList);

}
