package com.edc.erp.directly.returnnoticeorder.mapper;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货通知单与门店表(OrdDirReturnNoticeStore)表数据库访问层
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Repository
public interface OrdDirReturnNoticeStoreMapper extends BaseMapper<OrdDirReturnNoticeStore> {

    /**
     * 查询门店信息根据OrdDirReturnNoticeStore
     * @param ordDirReturnNoticeStore
     * @return
     */
    List<OrdReturnNoticeStoreOut> findStoreInfo(OrdDirReturnNoticeStore ordDirReturnNoticeStore);


    /**
     * 分页查门店信息
     * @param returnNoticeStoreIn
     * @return
     */
    List<OrdReturnNoticeStoreOut> findReturnNoticeStoresByPage(OrdReturnNoticeStoreIn returnNoticeStoreIn);

    /**
     * 查询最大限量退货时可退数量
     *
     * @param goodsCode
     * @param storeCode
     * @param id
     * @return
     */
    BigDecimal getMaxQtyByParameter(@Param("goodsCode")String goodsCode, @Param("storeCode") String storeCode, @Param("returnNoticeOrderId") Integer id);
}
