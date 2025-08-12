package com.edc.erp.directly.returnnoticeorder.service;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.directly.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;



/**
 * 退货通知单与商品表(OrdDirReturnNoticeGoods)表服务接口
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
public interface OrdDirReturnNoticeGoodsService extends BaseService<OrdDirReturnNoticeGoods> {

    /**
     * 根据退货通知单主键查单据下商品和门店信息
     * @param ordReturnNoticeIn
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findGoodsStoreInfoByReturnNoticeOrderId(OrdReturnNoticeDetailIn ordReturnNoticeIn);

    /**
     * 根据退货通知单主键删除商品信息
     * @param returnNoticeOrderId
     */
    void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId);

    /**
     * 根据退货通知单查id查商品
     * @param returnNoticeOrderId
     * @return
     */
    List<OrdDirReturnNoticeGoods> findByReturnNoticeId(Integer returnNoticeOrderId);

    /**
     * 导出商品信息
     * @param returnNoticeGoodsIn
     * @return
     */
    String exportOrdReturnGoods(ReturnNoticeGoodsIn returnNoticeGoodsIn);
}
