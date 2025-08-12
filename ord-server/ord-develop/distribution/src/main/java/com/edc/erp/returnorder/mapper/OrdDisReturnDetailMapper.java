package com.edc.erp.returnorder.mapper;

import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.model.in.OrdDisReturnDetailIn;
import com.edc.erp.returnorder.model.out.DisReturnOrderDtlPrintOut;
import com.edc.erp.returnorder.model.out.OrdDisReturnDetailOut;
import com.edc.erp.returnorder.model.out.OrdReturnDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货单详情表(DisReturnDetail)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-21 18:26:24
 */
@Repository
public interface OrdDisReturnDetailMapper extends BaseMapper<OrdDisReturnDetail> {

    /**
     *  统计申请退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    BigDecimal sumApplyReturnQuantity(@Param("returnOrderId") Integer returnOrderId);

    /**
     * 统计申请退货商品品项数
     *
     * @param returnOrderId
     * @return
     */
    Integer countApplyReturnSkuQuantity(@Param("returnOrderId") Integer returnOrderId);

    /**
     *统计实际退货商品总数量
     * @param returnOrderId
     * @return
     */
    BigDecimal sumActualReturnQuantity(@Param("returnOrderId") Integer returnOrderId);

    /**
     * 查询实际品项数
     * @param returnOrderId
     * @return
     */
    Integer countActualReturnSkuQuantity(@Param("returnOrderId") Integer returnOrderId);



    /**
     * 分页查询退货单明细
     *
     * @param pageIn
     * @return
     */
    List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDisReturnDetailIn pageIn);

    /**
     * 批量新增
     * @param returnDetails
     */
    void batchSave(@Param("returnDetails") List<OrdDisReturnDetail> returnDetails);

    /**
     * 批量修改
     * @param ordDisReturnDetailList
     * @return
     */
    int batchUpdate(@Param("ordDisReturnDetailList") List<OrdDisReturnDetail> ordDisReturnDetailList);

    /**
     * 获取退货单详情
     * @param pageIn
     * @return
     */
    List<OrdDisReturnDetailOut> finaOrdReturnDetail(OrdDisReturnDetailIn pageIn);


    List<DisReturnOrderDtlPrintOut> findPrintDtlByReturnId(@Param("returnOrderId") Long returnOrderId);
}
