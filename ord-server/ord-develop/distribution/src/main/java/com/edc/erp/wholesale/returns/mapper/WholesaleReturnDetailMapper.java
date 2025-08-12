package com.edc.erp.wholesale.returns.mapper;

import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 批发退货明细单(WholesaleReturnDetail)表数据库访问层
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Repository
public interface WholesaleReturnDetailMapper extends BaseMapper<WholesaleReturnDetail> {

    /**
     * 批量保存批发退货单详情
     *
     * @param list
     */
    void insertWholesaleReturnDetailList(@Param("list") List<WholesaleReturnDetail> list);

    /**
     * 根据批发退货单主键、商品代码、商品名称筛查批发退货单详情
     * @param wholesaleReturnId
     * @param goodsCode
     * @param goodsName
     * @return
     */
    List<WholesaleReturnDetail> findFilterWholesaleReturnDetailByPage(@Param("wholesaleReturnId") Long wholesaleReturnId,
                                                                      @Param("goodsCode") String goodsCode,@Param("goodsName") String goodsName);

    /**
     * 批量更新批发退货单明细
     * @param wholesaleReturnDetailList 批发退货单明细
     */
    void batchUpdate(@Param("detail") List<WholesaleReturnDetail> wholesaleReturnDetailList);

    WholesaleReturnDateInfoOut sumWholesaleReturnDateInfoByIdList(@Param("idList") List<Long> idList);

    void batchUpdateForAudit(@Param("detail") List<WholesaleReturnDetail> wholesaleReturnDetailList);
}
