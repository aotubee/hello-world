package com.edc.erp.returnorder.mapper;

import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.model.in.OrdReturnOrderPageIn;
import com.edc.erp.returnorder.model.out.AsyncExcelReturnOrderDetail;
import com.edc.erp.returnorder.model.out.BaseReturnOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货单(DisReturn)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Repository
public interface OrdDisReturnMapper extends BaseMapper<OrdDisReturn> {
    /**
     * 退货单分页
     * @param returnOrderPageIn
     * @return
     */
    List<BaseReturnOrderOut> findBaseReturnOrderByPage(OrdReturnOrderPageIn returnOrderPageIn);

    /**
     * 获取仓储库存
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal getInvNum(@Param("goodsCode") String goodsCode, @Param("stockCode")String stockCode,
                            @Param("wrhCode")String wrhCode);



    /**
     * 查询配销退货单列表(库存盘点)
     * @param ordDisReturn
     * @return
     */
    List<BaseReturnOrderOut> findDisReturnOrder(OrdDisReturn ordDisReturn);

    /**
     * 获取门店库存
     * @param goodsCode
     * @param bizOrgCode
     * @param storeCode
     * @return
     */
    BigDecimal getInvStoreNum(@Param("goodsCode") String goodsCode,@Param("bizOrgCode") String bizOrgCode, @Param("storeCode") String storeCode);

    /**
     * 为惠之园导出配销退货单明细信息
     * @param ordReturnOrderPageIn
     * @return
     */
    List<AsyncExcelReturnOrderDetail> findListForAsyncExportByPage(OrdReturnOrderPageIn ordReturnOrderPageIn);
}
