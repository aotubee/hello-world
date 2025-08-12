package com.edc.erp.returnorder.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.model.in.AppSaveOrdDisReturnDetailIn;
import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.returnorder.model.in.OrdDisReturnDetailIn;
import com.edc.erp.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.returnorder.model.out.DisReturnOrderDtlPrintOut;
import com.edc.erp.returnorder.model.out.OrdDisReturnDetailOut;
import com.edc.erp.returnorder.model.out.OrdReturnDetailOut;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 退货单详情表(DisReturnDetail)表服务接口
 *
 * @author yaojinpeng
 * @since 2022-10-21 18:26:16
 */
public interface OrdDisReturnDetailService extends BaseService<OrdDisReturnDetail> {

    /**
     * 统计申请退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    BigDecimal sumApplyReturnQuantity(Integer returnOrderId);

    /**
     *  统计申请退货商品品项数
     *
     * @param returnOrderId
     * @return
     */
    Integer countApplyReturnSkuQuantity(Integer returnOrderId);

    /**
     * 统计实际退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    BigDecimal sumActualReturnQuantity(Integer returnOrderId);

    /**
     * 统计实际品项数
     * @param returnOrderId
     * @return
     */
    Integer countActualReturnSkuQuantity(Integer returnOrderId);

    /**
     * 导出退货单明细
     * @param pageIn
     * @return
     */
    String export(OrdDisReturnDetailIn pageIn);

    /**
     * 分页查询退货单明细
     *
     * @param pageIn
     * @return
     */
    List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDisReturnDetailIn pageIn);

    /**
     * 批量添加
     * @param returnDetails
     */
    void batchSave(List<OrdDisReturnDetail> returnDetails);

    /**
     * 删除退货单
     * @param ordDisReturn
     */
    void deleteByReturnOrderId(OrdDisReturn ordDisReturn);

    /**
     * 批量修改
     * @param ordDisReturnDetailList
     * @return
     */
    int batchUpdate(List<OrdDisReturnDetail> ordDisReturnDetailList);

    /**
     * 初始化退货单明细map
     * @param returnOrderDetailMap
     * @param storeOut
     * @param returnGoodsInfoIn
     * @param orderGoodsOut
     * @param  ordDisReturn
     */
    void initReturnOrderDetailMap(Map<String, List<AppSaveOrdDisReturnDetailIn>> returnOrderDetailMap, StoreOut storeOut, AppSaveOrdDisReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDisReturn ordDisReturn);

    /**
     * 新增明细
     * @param returnOrderDetail
     */
    void saveReturnOrderDetail(OrdDisReturnDetail returnOrderDetail);

    /**
     *  初始化退货单明细
     * @param storeOut
     * @param returnGoodsInfoIn
     * @param orderGoodsOut
     * @return
     */
    AppSaveOrdDisReturnDetailIn initReturnOrderDetail(StoreOut storeOut, AppSaveOrdDisReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDisReturn ordDisReturn);

    /**
     * 获取退货单明细
     * @param returnOrderDetailId
     * @return
     */
    OrdDisReturnDetail getReturnOrderDetailById(Integer returnOrderDetailId);

    /**
     * 保存或者更新退货单明细
     * @param ordDisReturnDetail
     */
    void saveOrUpdateReturnOrderDetail(OrdDisReturnDetail ordDisReturnDetail);

    /**
     * 查明细
     * @param pageIn
     * @return
     */
    List<OrdDisReturnDetailOut> finaOrdReturnDetail(OrdDisReturnDetailIn pageIn);

    /**
     * 获取退货单明细集合
     * @param item
     * @return
     */

    List<OrdDisReturnDetail> findByReturnOrderId(Integer item);

    /**
     * App根据退货单明细主键删除一个明细
     * @param id
     */
    void deleteReturnOrderDetailById(Integer id);


    List<DisReturnOrderDtlPrintOut> findPrintDtlByReturnId(Long returnOrderId);

    @Transactional(rollbackFor = Exception.class)
    void handleDetail(OrdDisReturn ordDisReturn, OrdSaveReturnOrderIn saveReturnOrderIn);

    OrdReturnDetailOut compute(OrdDisReturnDetail ordDisReturnDetail, String storeCode, String bizOrgCode, String wrhCode, String stockCode, String centerStockBizOrgCode);

    OrdDisReturnDetail initDetail(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode, OrderGoodsOut orderGoodsOut);

    @Transactional(rollbackFor = Exception.class)
    void saveAsyncImportReturn(List<OrdSaveReturnOrderIn> returnOrderInList);
}
