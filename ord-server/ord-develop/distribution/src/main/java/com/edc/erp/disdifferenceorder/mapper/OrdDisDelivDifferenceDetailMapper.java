package com.edc.erp.disdifferenceorder.mapper;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


/**
 * 配销差异单详细表(OrdDisDelivDifferenceDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-24 11:16:38
 */
@Repository
public interface OrdDisDelivDifferenceDetailMapper extends BaseMapper<OrdDisDelivDifferenceDetail> {
    /**
     * 查询配销差异单明细
     * @param disDifferenceDetailIn
     * @return
     */
    List<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlListByPage(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn);
    /**
     * 批量新增配销差异单
     * @param disDelivDifferenceDetails
     */
    void batchSave(@Param("disDelivDifferenceDetails") List<OrdDisDelivDifferenceDetail> disDelivDifferenceDetails);

    /**
     * 批量修改配销差异单
     * @param ordDisDelivDifferenceDetails
     */
    void batchUpdate(@Param("ordDisDelivDifferenceDetails") List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails);

    /**
     * 查询商品品项数
     * @param id
     * @return
     */
    Integer getGoodsSize(Integer id);

    /**
     * 根据差异单主键查询详情
     * @param diffOrderId
     * @return
     */
    List<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlById(@Param("diffOrderId") Integer diffOrderId);
}
