package com.edc.erp.directly.dirfirstorder.mapper;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 铺货单明细表(OrdDirOrderFirstDetail)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Repository
public interface OrdDirOrderFirstDetailMapper extends BaseMapper<OrdDirOrderFirstDetail> {
    /**
     * 批量保存铺货单明细
     * @param firstOrderDetailList
     * @param userName
     * @param firstOrderId
     */
    void batchInsertFirstOrderDetail(@Param("firstOrderDetailList") List<OrdDirOrderFirstDetail> firstOrderDetailList,@Param("userName") String userName,@Param("firstOrderId") Long firstOrderId);

    /**
     * 分页查询铺货单明细
     * @param ordDirOrderFirstDetailIn
     * @return
     */
    List<OrdDirOrderFirstDetailOut> findOrdDisOrderFirstDetailByPage(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn);

    int batchUpdateNum(@Param("detailList") List<OrdDirOrderFirstDetail> detailList);

    int batchDelete(@Param("detailList") List<OrdDirOrderFirstDetail> detailList);
}
