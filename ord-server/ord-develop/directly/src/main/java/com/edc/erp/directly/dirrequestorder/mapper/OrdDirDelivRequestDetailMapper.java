package com.edc.erp.directly.dirrequestorder.mapper;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderDtlPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 要货单明细表(OrdDirDelivRequestDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Repository
public interface OrdDirDelivRequestDetailMapper extends BaseMapper<OrdDirDelivRequestDetail> {
    /**
     * 根据要货单ID查询要货单商品数量总和
     * @param requestOrderId
     * @return
     */
    BigDecimal getSkuNumberById(Long requestOrderId);

    /**
     * 获取要货单明细
     * @param dtlPageIn
     * @return
     */
    List<DirRequestOrderDetailOut> findRequestOrderDetailListByPage(DirRequestOrderDtlPageIn dtlPageIn);

    /**
     * 批量新增要货单详情表
     *
     * @param requestOrderDetailList
     */
    void batchSave(@Param("requestOrderDetailList") List<OrdDirDelivRequestDetail> requestOrderDetailList);

    /**
     * 根据要货单ID查询要货单商品品项数
     *
     * @param requestOrderId
     * @return
     */
    Integer getGoodsItemNumber(Long requestOrderId);
}
