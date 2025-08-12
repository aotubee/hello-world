package com.edc.erp.disfirstorder.mapper;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstDetailIn;
import com.edc.erp.disfirstorder.model.out.OrdDisOrderFirstDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销铺货单明细表(OrdDisOrderFirstDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
@Repository
public interface OrdDisOrderFirstDetailMapper extends BaseMapper<OrdDisOrderFirstDetail> {
    /**
     * 获取配销铺货单明细列表
     * @param ordDisOrderFirstDetailIn
     * @return
     */
    List<OrdDisOrderFirstDetailOut> findOrdDisOrderFirstDetailByPage(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn);

    /**
     * 批量新增铺货单明细
     * @param firstOrderDetailList
     * @param userName
     * @param firstOrderId
     */
    void batchInsertFirstOrderDetail(@Param("firstOrderDetailList") List<OrdDisOrderFirstDetail> firstOrderDetailList, @Param("userName") String userName, @Param("firstOrderId") Long firstOrderId);

    int batchUpdateNum(@Param("detailList") List<OrdDisOrderFirstDetail> detailList);

    int batchDelete(@Param("detailList") List<OrdDisOrderFirstDetail> detailList);
}
