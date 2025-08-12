package com.edc.erp.directly.returnorder.service;

import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.model.in.*;
import com.edc.erp.directly.returnorder.model.out.BaseReturnOrderOut;
import com.edc.erp.directly.returnorder.model.out.DirReturnOrderPrintOut;
import com.edc.erp.directly.returnorder.model.out.ReturnDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.UnificationReBillDtlVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 退货单(OrdDirReturn)表服务接口
 *
 * @author
 * @since 2022-11-18 18:49:55
 */
public interface OrdDirReturnService extends BaseService<OrdDirReturn> {

    /**
     * 保存退货单
     *
     * @param saveReturnOrderIn
     * @param isCheckRepeat
     * @return
     */
    Response saveOrUpdateReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn, boolean isCheckRepeat, String channelBizOrgCode);

    /**
     * 分页查退货单列表
     *
     * @param returnOrderPageIn
     * @return
     */
    Page<BaseReturnOrderOut> findBaseReturnOrderForPage(OrdReturnOrderPageIn returnOrderPageIn);

    /**
     * 查退货单明细
     *
     * @param pageIn
     * @return
     */
    Response<ReturnDetailOut> findOrdReturnDetails(OrdDirReturnDetailIn pageIn);

    /**
     * 审核退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    Response auditOrdReturn(OrdSaveReturnOrderIn saveReturnOrderIn, String centerStockBizOrgCode);

    /**
     * 作废退货单
     *
     * @param ordDirReturn
     * @return
     */
    Response invalidatedOrdReturn(OrdDirReturn ordDirReturn);

    /**
     * @Description: 收货
     * @Author: ZhangYao
     * @Date: 2024/10/31 17:32
     * @param returnGoodsInfoInList:
     * @param ordDirReturn:
     * @param stockInfoOut:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response receiving(List<OrdDirReturnDetail> returnGoodsInfoInList,OrdDirReturn ordDirReturn,StockInfoOut stockInfoOut);

    /**
     * 冲销退货单
     *
     * @param chargeReturnOrderIn
     * @return
     */
    int chargeReturnOrder(ChargeReturnOrderIn chargeReturnOrderIn);

    /**
     * 导入商品明细
     *
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @param warehouseCode
     * @param stockCode
     * @return
     */
    Response importReturnOrderGoods(String fileId, String storeCode, String bizOrgCode, String warehouseCode, String stockCode, String distributionType,String deliveryOrderNo,  String centerStockBizOrgCode);

    /**
     * 批量审核
     *
     * @param returnOrderIds
     * @return
     */
    int batchApproved(List<Integer> returnOrderIds);

    /**
     * 导出退货单
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    String exportOrdReturn(OrdReturnOrderPageIn ordReturnOrderPageIn);
//
//    /**
//     * 导入退货单及明细
//     *
//     * @param fileId
//     * @param bizOrgCode
//     * @return
//     */
//    Response importOrdReturn(String fileId, String bizOrgCode);

    /**
     * 初始退货单
     *
     * @param ordDirReturn
     * @param detail
     * @return
     */
    List<OrdDirReturnDetail> initReturnOrder(OrdDirReturn ordDirReturn, List<UnificationReBillDtlVO> detail, String centerBizOrgCode);

    /**
     * 释放库存
     *
     * @param ordDirReturn
     * @param ordDisReturnDetailList
     */
    void adjustInv(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> ordDisReturnDetailList, StockInfoOut stockInfoOut, LocalDateTime flowDate);

    /**
     * 查退货商品信息
     *
     * @param orderGoodsIn
     * @return
     */
    Response<SaveReturnGoodsOut> getGoodsInfo(OrderGoodsIn orderGoodsIn, String centerStockBizOrgCode);

    /**
     * 根据退货单id查退货单
     *
     * @param id
     * @return
     */
    OrdDirReturn getReturnOrderById(Integer id);

    /**
     * 退货通知单保存退货单
     *
     * @param ordSaveReturnOrderIn
     * @return
     */
    Response saveAndUpdateReturnOrderByReturnNotice(AppDirReturnOrderSaveIn ordSaveReturnOrderIn);

    /**
     * 查询直营退货单列表(库存盘点)
     *
     * @param ordDirReturn
     * @return
     */
    List<BaseReturnOrderOut> findDirReturnOrder(OrdDirReturn ordDirReturn);

    Response<String> asyncImportReturn(String fileId, String loginUsername, String bizOrgCode);

    /**
     * app提交退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    Response<String> submitReturnOrderForApp(OrdSaveReturnOrderIn saveReturnOrderIn);

    /**
     * 删除退货单
     *
     * @param id
     */
    void deleteByReturnOrderById(Integer id);

    /**
     * 根据id，组织代码查询退货单
     *
     * @param returnOrderId
     * @param bizOrgCode
     * @return
     */
    OrdDirReturn getReturnOrderByIdAndOrgCode(Integer returnOrderId, String bizOrgCode);

    /**
     * 退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO);

    /**
     * 导出多个配货退货单明细
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    String exportOrdReturnDetailByOrder(OrdReturnOrderPageIn ordReturnOrderPageIn);

    /**
     * 根据退货单NO查退货单
     *
     * @param orderNo
     * @return
     */
    OrdDirReturn getReturnOrderByNo(String orderNo);

    Response<String> batchInvalidatedOrdReturn(List<Integer> idList);

    Response<String> batchReceiving(List<Integer> idList);

    List<DirReturnOrderPrintOut> findPrintDataByIds(List<Long> ids, String bizOrgCode);
}
