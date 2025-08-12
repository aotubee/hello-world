package com.edc.erp.returnnoticeorder.mapper;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeGoods;
import com.edc.erp.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 退货通知单与商品表(DisReturnNoticeGoods)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:55
 */
@Repository
public interface OrdDisReturnNoticeGoodsMapper extends BaseMapper<OrdDisReturnNoticeGoods> {
    /**
     * 获取退货通知单与商品分页
     * @param returnNoticeGoodsIn
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findGoodsOutByPage(ReturnNoticeGoodsIn returnNoticeGoodsIn);

    /**
     * 状态开关
     * @param statusId
     * @param bizOrgCode
     * @return
     */
    int getGoodsStatusBusinessSwitchByStatusId(@Param("statusId") Integer statusId, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件获取退货通知单与商品
     * @param returnNoticeId
     * @return
     */
    List<OrdDisReturnNoticeGoods> findByReturnNoticeId(@Param("returnNoticeId")Integer returnNoticeId);

    /**
     * 根据条件获取退货通知单与商品
     * @param returnNoticeId
     * @param goodsCode
     * @return
     */
    OrdDisReturnNoticeGoods getByReturnNoticeIdAndGoodsCode(@Param("returnNoticeId") Integer returnNoticeId, @Param("goodsCode") String goodsCode);
}
