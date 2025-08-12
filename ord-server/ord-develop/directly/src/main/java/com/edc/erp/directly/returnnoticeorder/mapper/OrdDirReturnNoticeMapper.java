package com.edc.erp.directly.returnnoticeorder.mapper;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderDetailOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 退货通知单表(OrdDirReturnNotice)表数据库访问层
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Repository
public interface OrdDirReturnNoticeMapper extends BaseMapper<OrdDirReturnNotice> {

    /**
     * 分业查询退货通知单
     * @param ordReturnNoticeIn
     * @return
     */
    List<OrdReturnNoticeOrderOut> findOrdReturnNoticeByPage(OrdReturnNoticeIn ordReturnNoticeIn);

    /**
     * 查询退货生效时间的退货通知单
     * @param returnNotice
     * @return
     */
    List<OrdDirReturnNotice> selectTakeEffectTime(OrdDirReturnNotice returnNotice);

    /**
     * 根据单号校验退货通知单是否存在以及是否已生效状态
     * @param returnNoticeOrderNo
     * @param bizOrgCode
     * @return
     */
    OrdDirReturnNotice checkReturnNoticeNo(@Param("returnNoticeOrderNo") String returnNoticeOrderNo,@Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据退货单查门店信息
     * @param returnNoticeOrderId
     * @param storeCode
     * @return
     */
    OrdReturnNoticeOrderDetailOut selectOneByIdAndStoreCode(@Param("returnNoticeOrderId") Integer returnNoticeOrderId,
                                                            @Param("storeCode") String storeCode);

    /**
     * 查商品信息
     * @param returnNoticeOrderId
     * @param storeCode
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findReturnNoticeGoodsByReturnNoticeOrderId(
            @Param("returnNoticeOrderId") Integer returnNoticeOrderId, @Param("storeCode") String storeCode);

    /**
     * 查询未退生效通知单的数量
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    int getUnreturnedCount(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode);
}
