package com.edc.erp.returnnoticeorder.service;


import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeGoods;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 退货通知单与商品表(DisReturnNoticeGoods)表服务接口
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:55
 */
public interface OrdReturnNoticeGoodsService extends BaseService<OrdDisReturnNoticeGoods> {

    /**
     * 退货通知单下商品信息列表
     *
     * @param returnNoticeGoodsIn 商品信息入参
     * @return
     */
    Page<OrdReturnNoticeGoodsOut> findReturnNoticeGoodsOutForPage(ReturnNoticeGoodsIn returnNoticeGoodsIn);

    /**
     * 导退货通知单商品明细
     *
     * @param returnNoticeGoodsIn
     * @return
     */
    String exportOrdReturnGoods(ReturnNoticeGoodsIn returnNoticeGoodsIn);

    /**
     * 退后通知单导入商品明细
     * @param fileId
     * @param bizOrgCode
     * @return
     */
    Response<List<OrdReturnNoticeGoodsOut>> importReturnNoticeGoods(String fileId,List<String> goodsCodes, String bizOrgCode);

    /**
     * 校验商品是否符合
     * @param goodsCode
     * @return
     */
    Response<OrdReturnNoticeGoodsOut> checkGoodsCode(String goodsCode);

    /**
     * 根据主键删除商品信息
     * @param returnNoticeOrderId
     */
    void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId);

    /**
     * 查商品和门店信息
     * @param ordReturnNoticeIn
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findGoodsStoreInfoByReturnNoticeOrderId(OrdReturnNoticeDetailIn ordReturnNoticeIn);
}
