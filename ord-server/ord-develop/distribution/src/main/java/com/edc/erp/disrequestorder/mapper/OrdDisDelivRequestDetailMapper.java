package com.edc.erp.disrequestorder.mapper;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderDtlPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 集货单明细表(OrdDisDelivRequestDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Repository
public interface OrdDisDelivRequestDetailMapper extends BaseMapper<OrdDisDelivRequestDetail> {
    /**
     * 根据集货单ID查询集货单商品数量总和
     * @param requestOrderId
     * @return
     */
    BigDecimal getSkuNumberById(@Param("requestOrderId") Long requestOrderId);

    /**
     * 根据集货单ID查询集货单商品品项数
     * @param requestOrderId
     * @return
     */
    Integer getGoodsItemNumber(@Param("requestOrderId") Long requestOrderId);

    /**
     *根据集货单主键查询集货单明细
     * @param dtlPageIn
     * @return
     */
    List<BackRequestOrderDetailOut> findRequestOrderDetailListByPage(BackQueryRequestOrderDtlPageIn dtlPageIn);

    /**
     * 批量新增集货单详情表
     *
     * @param requestOrderDetailList
     */
    void batchSave(@Param("requestOrderDetailList") List<OrdDisDelivRequestDetail> requestOrderDetailList);
}
