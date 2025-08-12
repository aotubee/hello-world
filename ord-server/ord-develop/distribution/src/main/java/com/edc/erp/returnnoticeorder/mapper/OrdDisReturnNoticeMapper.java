package com.edc.erp.returnnoticeorder.mapper;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeIn;
import com.edc.erp.returnnoticeorder.model.out.OrdBackHeaderReturnNoticeOrderOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 退货通知单表(DisReturnNotice)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:53
 */
@Repository
public interface OrdDisReturnNoticeMapper extends BaseMapper<OrdDisReturnNotice> {

    /**
     * 分业查询退货通知单
     *
     * @param ordReturnNoticeIn
     * @return
     */
    List<OrdReturnNoticeOrderOut> findOrdReturnNoticeByPage(OrdReturnNoticeIn ordReturnNoticeIn);

    /**
     * 查询退货生效时间的退货通知单
     * @param returnNotice
     * @return
     */
    List<OrdDisReturnNotice> selectTakeEffectTime(OrdDisReturnNotice returnNotice);

    /**
     * 校验退货通知单是否存在
     * @param returnNoticeOrderNo
     * @param bizOrgCode
     * @return
     */
    OrdDisReturnNotice checkReturnNoticeNo(@Param("returnNoticeOrderNo") String returnNoticeOrderNo, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 退货通知单id和门店code查商品信息
     * @param returnNoticeOrderId
     * @param storeCode
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findReturnNoticeGoodsByReturnNoticeOrderId(
            @Param("returnNoticeOrderId") Integer returnNoticeOrderId, @Param("storeCode") String storeCode);

    /**
     * g根据退货通知单主键和门店查信息
     * @param returnNoticeOrderId
     * @param storeCode
     * @return
     */
    OrdBackHeaderReturnNoticeOrderOut selectOneByIdAndStoreCode(@Param("returnNoticeOrderId") Integer returnNoticeOrderId,@Param("storeCode") String storeCode);


//    List<OrdReturnNoticeOrderOut> findOrdReturnNoticeByPage(OrdReturnNoticeIn ordReturnNoticeIn);
//
//    BaseReturnNoticeOrder selectOneByIdAndStoreCode(@Param(value = "id") Integer id, @Param(value = "storeCode") String storeCode);
//
//    /**
//     * 查询所有退货通知单创建者
//     *
//     * @return
//     */
//    List<String> findAllReturnNoticeOrderCreatorList();

    /**
     * 查询未退生效通知单的数量
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    int getUnreturnedCount(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode);
}
