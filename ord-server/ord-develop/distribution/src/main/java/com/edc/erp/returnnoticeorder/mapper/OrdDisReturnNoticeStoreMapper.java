package com.edc.erp.returnnoticeorder.mapper;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeStore;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货通知单与门店表(DisReturnNoticeStore)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:56
 */
@Repository
public interface OrdDisReturnNoticeStoreMapper extends BaseMapper<OrdDisReturnNoticeStore> {

    /**
     * 分页查询门店信息
     * @param returnNoticeStoreIn
     * @return
     */
    List<OrdReturnNoticeStoreOut> findPpReturnNoticeStoresByPage(OrdReturnNoticeStoreIn returnNoticeStoreIn);

    /**
     * 根据退货通知单id和商品id查门店信息
     * @param returnNoticeId
     * @param returnNoticeGoodsId
     * @return
     */
    List<OrdReturnNoticeStoreOut> findByReturnNoticeIdAndReturnGoodsId(@Param("returnNoticeId") Integer returnNoticeId, @Param("returnNoticeGoodsId") Integer returnNoticeGoodsId);

    /**
     * 查门店信息
     * @param ordDisReturnNoticeStore
     * @return
     */
    List<OrdReturnNoticeStoreOut> findStoreInfo(OrdDisReturnNoticeStore ordDisReturnNoticeStore);

    /**
     * 获取最大最大申请数
     * @param goodsCode
     * @param storeCode
     * @param id
     * @return
     */
    BigDecimal getMaxQtyByParameter(@Param("goodsCode")String goodsCode, @Param("storeCode") String storeCode,@Param("returnNoticeOrderId") Integer id);


}
