package com.edc.erp.directly.returnorder.mapper;

import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.model.excel.AsyncExcelReturnOrderDetail;
import com.edc.erp.directly.returnorder.model.in.OrdReturnOrderPageIn;
import com.edc.erp.directly.returnorder.model.out.BaseReturnOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货单(OrdDirReturn)表数据库访问层
 *
 * @author
 * @since 2022-11-18 18:50:02
 */
@Repository
public interface OrdDirReturnMapper extends BaseMapper<OrdDirReturn> {

    /**
     * 分页查退货单明细
     * @param returnOrderPageIn
     * @return
     */
    List<BaseReturnOrderOut> findBaseReturnOrderByPage(OrdReturnOrderPageIn returnOrderPageIn);

    /**
     * 查仓储库存数量
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal getInvNum(@Param("goodsCode") String goodsCode, @Param("stockCode") String stockCode,
                       @Param("wrhCode") String wrhCode);

    /**
     * 查询直营退货单列表(库存盘点)
     * @param ordDirReturn
     * @return
     */
    List<BaseReturnOrderOut> findDirReturnOrder(OrdDirReturn ordDirReturn);

    /**
     * 校验门店库存数量
     * @param goodsCode
     * @param bizOrgCode
     * @param storeCode
     * @return
     */
    BigDecimal getInvStoreNum(@Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode, @Param("storeCode") String storeCode);

    /**
     * 为惠之园导出配货退货单明细信息
     * @param ordReturnOrderPageIn
     * @return
     */
    List<AsyncExcelReturnOrderDetail> findListForAsyncExportByPage(OrdReturnOrderPageIn ordReturnOrderPageIn);
}
