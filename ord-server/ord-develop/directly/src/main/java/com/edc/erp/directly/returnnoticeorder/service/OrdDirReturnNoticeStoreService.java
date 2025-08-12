package com.edc.erp.directly.returnnoticeorder.service;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;



/**
 * 退货通知单与门店表(OrdDirReturnNoticeStore)表服务接口
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
public interface OrdDirReturnNoticeStoreService extends BaseService<OrdDirReturnNoticeStore> {

    /**
     * 根据退货通知单主键和商品信息主键查门店
     * @param ordReturnNoticeIn
     * @param id
     * @return
     */
    List<OrdReturnNoticeStoreOut> findStoreInfo(OrdReturnNoticeDetailIn ordReturnNoticeIn, Integer id);

    /**
     * 根据退货通知单主键删除门店信息
     * @param returnNoticeOrderId
     */
    void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId);

    /**
     * 根据退货通知单主键和商品主键查门店信息
     * @param returnNoticeOrderId
     * @param id
     * @return
     */
    List<OrdReturnNoticeStoreOut> findByReturnNoticeIdAndReturnGoodsId(Integer returnNoticeOrderId, Integer id);

    /**
     * 导出门店信息
     * @param returnNoticeStoreIn
     * @return
     */
    String exportOrdReturnStore(OrdReturnNoticeStoreIn returnNoticeStoreIn);
}
