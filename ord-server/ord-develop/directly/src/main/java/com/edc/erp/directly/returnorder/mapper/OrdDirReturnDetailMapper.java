package com.edc.erp.directly.returnorder.mapper;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.model.in.OrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.out.DirReturnOrderDtlPrintOut;
import com.edc.erp.directly.returnorder.model.out.OrdDirReturnDetailOut;
import com.edc.erp.directly.returnorder.model.out.OrdReturnDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货单详情表(OrdDirReturnDetail)表数据库访问层
 *
 * @author
 * @since 2022-11-18 18:50:07
 */
@Repository
public interface OrdDirReturnDetailMapper extends BaseMapper<OrdDirReturnDetail> {

    /**
     * 批量添加明细
     * @param returnDetails
     */
    void batchSave(@Param("returnDetails") List<OrdDirReturnDetail> returnDetails);

    /**
     * 查询退货单明细
     * @param pageIn
     * @return
     */
    List<OrdDirReturnDetailOut> finaOrdReturnDetail(OrdDirReturnDetailIn pageIn);

    /**
     * 批量修改退货单明细
     * @param ordDirReturnDetailList
     * @return
     */
    int batchUpdate(@Param("ordDirReturnDetailList") List<OrdDirReturnDetail> ordDirReturnDetailList);

    /**
     * 分页查询退货单明细
     * @param pageIn
     * @return
     */
    List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDirReturnDetailIn pageIn);

    /**
     * 统计实际退货商品品项数
     * @param returnOrderId
     * @return
     */
    Integer countActualReturnSkuQuantity(@Param("returnOrderId") Integer returnOrderId);

    /**
     * 统计申请退货商品总数量
     * @param returnOrderId
     * @return
     */
    BigDecimal sumApplyReturnQuantity(@Param("returnOrderId") Integer returnOrderId);

    /**
     * 统计申请退货商品品项数
     * @param returnOrderId
     * @return
     */
    Integer countApplyReturnSkuQuantity(@Param("returnOrderId")Integer returnOrderId);

    /**
     *统计实际退货商品总数量
     * @param returnOrderId
     * @return
     */
    BigDecimal sumActualReturnQuantity(@Param("returnOrderId")Integer returnOrderId);

    List<DirReturnOrderDtlPrintOut> findPrintDtlByReturnId(@Param("returnOrderId") Long returnOrderId);
}
