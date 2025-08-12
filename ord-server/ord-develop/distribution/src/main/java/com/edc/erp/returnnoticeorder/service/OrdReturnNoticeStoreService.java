package com.edc.erp.returnnoticeorder.service;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeStore;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.List;


/**
 * 退货通知单与门店表(DisReturnNoticeStore)表服务接口
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:56
 */
public interface OrdReturnNoticeStoreService extends BaseService<OrdDisReturnNoticeStore> {

    /**
     * 分页查询退货通知单下门店信息
     *
     * @param returnNoticeStoreIn 退货通知门店入参查询类
     * @return
     */
    Page<OrdReturnNoticeStoreOut> findReturnNoticeStoreOutForPage(OrdReturnNoticeStoreIn returnNoticeStoreIn);

    /**
     * 导出退货通知单门店明细
     *
     * @param returnNoticeStoreIn
     * @return
     */
    String exportOrdReturnStore(OrdReturnNoticeStoreIn returnNoticeStoreIn);

    /**
     * 退货通知单导入门店明细
     *
     * @param fileId
     * @param bizOrgCode
     * @param returnType
     * @return
     */
    Response<List<OrdReturnNoticeStoreOut>> importReturnNoticeStore(String fileId, String bizOrgCode,String returnType);

    /**
     * 根据退货通知单id删除门店信息
     * @param returnNoticeOrderId
     */
    void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId);

    /**
     * 查门店信息
     * @param ordReturnNoticeIn
     * @param returnNoticeGoodsId
     * @return
     */
    List<OrdReturnNoticeStoreOut> findStoreInfo(OrdReturnNoticeDetailIn ordReturnNoticeIn, Integer returnNoticeGoodsId);

    /**
     * 查询最大申请数
     * @param goodsCode
     * @param storeCode
     * @param id
     * @return
     */
    BigDecimal getMaxQtyByParameter(String goodsCode, String storeCode, Integer id);
}
