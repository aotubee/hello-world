package com.edc.erp.returnorder.service;

import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.model.in.*;
import com.edc.erp.returnorder.model.out.BaseReturnOrderOut;
import com.edc.erp.returnorder.model.out.DisReturnOrderPrintOut;
import com.edc.erp.returnorder.model.out.ReturnDetailOut;
import com.edc.erp.returnorder.model.out.SaveReturnGoodsOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.UnificationReBillDtlVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 退货单(DisReturn)表服务接口
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
public interface OrdDisReturnService extends BaseService<OrdDisReturn> {

    /**
     * 新增退货单
     *
     * @param ordDisReturn
     * @return
     */
    int saveOrdDisReturn(OrdDisReturn ordDisReturn);

    /**
     * 根据主键查询退货订单
     *
     * @param returnOrderId
     * @return
     */
    OrdDisReturn getReturnOrderById(Integer returnOrderId);

    /**
     * 根据主键和id查询退货单
     *
     * @param returnOrderId
     * @param bizOrgCode
     * @return
     */
    OrdDisReturn getReturnOrderByIdAndOrgCode(Integer returnOrderId, String bizOrgCode);


    /**
     * 分页查询退货单列表
     *
     * @param returnOrderPageIn
     * @return
     */
    Page<BaseReturnOrderOut> findBaseReturnOrderForPage(OrdReturnOrderPageIn returnOrderPageIn);

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
     * @param returnOrder
     */
    void invalidatedOrdReturn(OrdDisReturn returnOrder);


    /**
     * 退货导入监听器
     *
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @param warehouseCode
     * @param stockCode
     * @return
     */
    Response importReturnOrderGoods(String fileId, String storeCode, String bizOrgCode, String warehouseCode, String stockCode,
                                    String distributionType, String deliveryOrderNo, String centerStockBizOrgCode);

    /**
     * 冲销退货单
     *
     * @param chargeReturnOrderIn
     * @return
     */
    int chargeReturnOrder(ChargeReturnOrderIn chargeReturnOrderIn);

    /**
     * @Description: 保存退货单
     * @Author: ZhangYao
     * @Date: 2024/4/17 17:52
     * @param saveReturnOrderIn:
     * @param isCheckRepeat:
     * @param channelBizOrgCode:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response saveOrUpdateReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn, boolean isCheckRepeat, String channelBizOrgCode);

    Response<String> asyncImportReturn(String fileId, String loginUsername, String loginBizOrgCode);

    /**
     * c初始化退货单详情
     *
     * @param ordDisReturn
     * @param detail
     * @return
     */
    List<OrdDisReturnDetail> initReturnOrder(OrdDisReturn ordDisReturn, List<UnificationReBillDtlVO> detail, String centerBizOrgCode);


//    /**
//     * 释放库存 资金
//     * @param ordDisReturn
//     * @param ordDisReturnDetailList
//     */
//    void releaseStockAndFund(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> ordDisReturnDetailList);

    /**
     * 释放库存
     *
     * @param ordDisReturn
     * @param ordDisReturnDetailList
     */
    void adjustInv(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> ordDisReturnDetailList, StockInfoOut stockInfoOut, LocalDateTime flowDate);

    /**
     * 资金调整
     *
     * @param ordDisReturn
     */
    void receivingReturnOrderToFund(OrdDisReturn ordDisReturn);

    /**
     * 退货通知单保存退货单
     *
     * @param appDisReturnOrderSaveIn
     * @return
     */
    Response saveReturnOrderByReturnNotice(AppDisReturnOrderSaveIn appDisReturnOrderSaveIn);

    /**
     * @Description: 收货
     * @Author: ZhangYao
     * @Date: 2024/10/31 17:32
     * @param returnGoodsInfoInList:
     * @param returnOrder:
     * @param stockInfoOut:
     * @return: com.edc.plugins.common.response.Response
     **/
    Response receiving(List<OrdDisReturnDetail> returnGoodsInfoInList, OrdDisReturn returnOrder, StockInfoOut stockInfoOut);

    /**
     * 查询退货单明细
     *
     * @param pageIn
     * @return
     */
    Response<ReturnDetailOut> findOrdReturnDetails(OrdDisReturnDetailIn pageIn);


    /**
     * 批量批准
     *
     * @param returnOrderIds
     * @return
     */
    int batchApproved(List<Integer> returnOrderIds);

    /**
     * 导出退货列表
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    String exportOrdReturn(OrdReturnOrderPageIn ordReturnOrderPageIn);

    /**
     * 导入退货单列表
     *
     * @param fileId
     * @param bizOrgCode
     * @return
     */
//    Response importOrdReturn(String fileId, String bizOrgCode);

    /**
     * 退货商品信息
     *
     * @param orderGoodsIn
     * @return
     */
    Response<SaveReturnGoodsOut> getGoodInfo(OrderGoodsIn orderGoodsIn, String centerStockBizOrgCode);


    /**
     * 初始化退货单发dts
     *
     * @param ordDisReturn
     * @param returnGoodsInfoInList
     */

    void initReturnOrderToDts(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnGoodsInfoInList, String centerStockBizOrgCode);

    /**
     * 查询配销退货单列表(库存盘点)
     *
     * @param ordDisReturn
     * @return
     */
    List<BaseReturnOrderOut> findDisReturnOrder(OrdDisReturn ordDisReturn);

    /**
     * App提交退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    Response<String> submitReturnOrderForApp(OrdSaveReturnOrderIn saveReturnOrderIn);

    /**
     * 根据退货主键删除退货单
     *
     * @param id
     */
    void deleteByReturnOrderById(Integer id);

    /**
     * 配销退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO);

    /**
     * 导出多个配销退货单明细
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    String exportOrdReturnDetailByOrder(OrdReturnOrderPageIn ordReturnOrderPageIn);

    /**
     * 铜鼓单号查询退货单
     *
     * @param orderNo
     * @return
     */
    OrdDisReturn getReturnOrderByNo(String orderNo);

    Response<String> batchInvalidatedOrdReturn(List<Integer> idList);

    Response<String> batchReceiving(List<Integer> idList, String loginUsername);

    OrdDisReturn getReturnOrderNoByIdAndBizOrgCode(String returnOrderNo, String bizOrgCode);

    List<DisReturnOrderPrintOut> findPrintDataByIds(List<Long> ids, String bizOrgCode);

}
