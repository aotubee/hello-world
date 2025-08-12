package com.edc.erp.wholesale.returns.mapper;

import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsListIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnsListOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 批发退货单(WholesaleReturns)表数据库访问层
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Repository
public interface WholesaleReturnsMapper extends BaseMapper<WholesaleReturns> {

    /**
     * 批发退货单列表查询
     *
     * @param wholesaleReturnsListIn
     * @return
     */
    List<WholesaleReturnsListOut> findWholesaleReturnsByPage(@Param("wholesaleReturnsListIn") WholesaleReturnsListIn wholesaleReturnsListIn);

    List<Long> findNeedSumWholesaleReturnIdList(@Param("wholesaleReturnsListIn") WholesaleReturnsListIn wholesaleReturnsListIn);

    int updateOrder(@Param("order") WholesaleReturns wholesaleReturns, @Param("beforeStatus") String beforeStatus);
}
