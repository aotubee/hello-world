package com.edc.erp.directly.returnorder.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.directly.returnorder.model.in.AppSaveOrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.in.OrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.directly.returnorder.model.out.DirReturnOrderDtlPrintOut;
import com.edc.erp.directly.returnorder.model.out.OrdDirReturnDetailOut;
import com.edc.erp.directly.returnorder.model.out.OrdReturnDetailOut;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 退货单详情表(OrdDirReturnDetail)表服务接口
 *
 * @author
 * @since 2022-11-18 18:50:07
 */
public interface OrdDirReturnDetailService extends BaseService<OrdDirReturnDetail> {

    /**
     * 批量添加明细
     * @param returnDetails
     */
    void batchSave(List<OrdDirReturnDetail> returnDetails);

    /**
     * 根据退货单主键删除退货单明细
     * @param ordDirReturn
     */
    void deleteByReturnOrderId(OrdDirReturn ordDirReturn);

    /**
     * 查退货通知单明细
     * @param pageIn
     * @return
     */
    List<OrdDirReturnDetailOut> finaOrdReturnDetail(OrdDirReturnDetailIn pageIn);

    /**
     * 批量修改
     * @param list
     * @return
     */
    int batchUpdate(List<OrdDirReturnDetail> list);

    /**
     * 导出退货明细
     * @param pageIn
     * @return
     */
    String export(OrdDirReturnDetailIn pageIn);

    /**
     * 根据退货单id查退货明细
     * @param item
     * @return
     */
    List<OrdDirReturnDetail> findByReturnOrderId(Integer item);

    /**
     * 分页查询退货单明细
     * @param ordDirReturnDetailIn
     * @return
     */
    List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDirReturnDetailIn ordDirReturnDetailIn);

    /**
     * 初始化退货单
     * @param returnOrderDetailMap
     * @param storeOut
     * @param appSaveOrdDirReturnDetailIn
     * @param orderGoodsOut
     * @param  ordDirReturn
     */
    void initReturnOrderDetailMap(Map<String, List<AppSaveOrdDirReturnDetailIn>> returnOrderDetailMap, StoreOut storeOut, AppSaveOrdDirReturnDetailIn appSaveOrdDirReturnDetailIn, OrderGoodsOut orderGoodsOut, OrdDirReturn ordDirReturn);

    /**
     * 保存退货单详情
     *
     * @param returnOrderDetail
     */
    void saveReturnOrderDetail(OrdDirReturnDetail returnOrderDetail);

    /**
     * 初始化退货单详情
     *
     * @param storeOut
     * @param returnGoodsInfoIn
     * @param orderGoodsOut
     * @return
     */
    AppSaveOrdDirReturnDetailIn initReturnOrderDetail(StoreOut storeOut, AppSaveOrdDirReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDirReturn ordDirReturn);

    /**
     * 根据id查询
     *
     * @param returnOrderDetailId
     * @return
     */
    OrdDirReturnDetail getReturnOrderDetailById(Integer returnOrderDetailId);

    /**
     * 保存或修改退货单明细
     *
     * @param ordDisReturnDetail
     */
    void saveOrUpdateReturnOrderDetail(OrdDirReturnDetail ordDisReturnDetail);

    /**
     * 根据退货单明细主键删除一个明细
     * @param id
     */
    void deleteReturnOrderDetailById(Integer id);

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


    List<DirReturnOrderDtlPrintOut> findPrintDtlByReturnId(Long returnOrderId);

    OrdDirReturnDetail initDetail(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode, OrderGoodsOut orderGoods);

    @Transactional(rollbackFor = Exception.class)
    void saveAsyncImportReturn(List<OrdSaveReturnOrderIn> returnOrderInList);

    @Transactional(rollbackFor = Exception.class)
    void handleReturnDetail(OrdDirReturn ordDirReturn, OrdSaveReturnOrderIn saveReturnOrderIn);

    /**
     * 计算数据
     *
     * @param ordDirReturnDetail
     * @return
     */
    OrdReturnDetailOut compute(OrdDirReturnDetail ordDirReturnDetail, String storeCode, String bizOrgCode, String wrhCode, String stockCode, String centerStockBizOrgCode);
}
