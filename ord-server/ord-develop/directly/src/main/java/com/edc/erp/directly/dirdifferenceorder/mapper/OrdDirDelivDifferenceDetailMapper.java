package com.edc.erp.directly.dirdifferenceorder.mapper;

import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.model.in.OrdDirDelivDifferenceDetailIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.OrdDirDelivDifferenceDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 差异单详细表(OrdDirDelivDifferenceDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-14 11:32:46
 */
@Repository
public interface OrdDirDelivDifferenceDetailMapper extends BaseMapper<OrdDirDelivDifferenceDetail> {
    /**
     * 查询商品品项数
     * @param id
     * @return
     */
    Integer getGoodsSize(@Param("id") Integer id);

    /**
     * 获取差异单详情
     * @param dirDifferenceDetailIn
     * @return
     */
    List<OrdDirDelivDifferenceDetailOut> findDifferenceOrderDtlListByPage(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn);

    /**
     * 批量新增差异单明细
     * @param details
     */
    void batchSave(@Param("details") List<OrdDirDelivDifferenceDetail> details);


    /**
     * 批量修改差异单明细
     *
     * @param ordDirDelivDifferenceDetails
     */
    void batchUpdate(@Param("ordDirDelivDifferenceDetails") List<OrdDirDelivDifferenceDetail> ordDirDelivDifferenceDetails);

    /**
     * 根据差异单主键查询差异单明细
     * @param diffOrderId
     * @return
     */
    List<OrdDirDelivDifferenceDetailOut> findDifferenceOrderDtlById(@Param("diffOrderId") Integer diffOrderId);
}
