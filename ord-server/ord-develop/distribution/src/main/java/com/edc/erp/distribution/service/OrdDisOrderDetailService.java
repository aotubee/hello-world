package com.edc.erp.distribution.service;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.model.in.OrderDetailIn;
import com.edc.erp.distribution.model.out.AppTopDisOrderGoodsOut;
import com.edc.erp.distribution.model.out.BackHeaderOrderDetailOut;
import com.edc.erp.distribution.model.out.OrderDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


/**
 * 配销订货单详细表(OrdDisOrderDetail)表服务接口
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
public interface OrdDisOrderDetailService extends BaseService<OrdDisOrderDetail> {

    /**
     * 删除存在的sku信息
     *
     * @param order
     */
    void removeSkuForExistsQuantity(OrdDisOrder order);

    /**
     * 根据订单id查询明细信息
     *
     * @param orderId
     * @return
     */
    List<OrdDisOrderDetail> findOrderDetailListByOrderId(Long orderId);

    /**
     * 保存配销订货单明细信息
     *
     * @param orderDetail
     */
    void save(OrdDisOrderDetail orderDetail);

    /**
     * 释放订单金额
     *
     * @param ordDisOrder
     * @return
     */
    Response releaseOrderAmount(OrdDisOrder ordDisOrder);

    /**
     * 查询订货单明细分页列表
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    Page<OrderDetailOut> findOrderDetailOutPage(OrderDetailIn orderDetailIn, String bizOrgCode);

    /**
     * 运营端查询订货单明细表头
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    BackHeaderOrderDetailOut getBackHeaderOrderDetailOut(Long orderId, String bizOrgCode);

    /**
     * 订货单明细列表导出
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    String exportOrderDetailOut(OrderDetailIn orderDetailIn, String bizOrgCode);

    /**
     * 根据订单id查询包装商品数量
     *
     * @param skuCode
     * @param orderIdList
     * @param appUserOut
     * @param truncationTimeStr
     * @return
     */
    BigDecimal sumPackageByOrderIdList(String skuCode, List<Long> orderIdList, AppUserOut appUserOut, String truncationTimeStr);

    /**
     * 校验订货量
     *
     * @param orderQuantity 本次订货总数
     * @param monthlySales  月销量
     * @return
     */
    int getIsNeedOrderQuantityWarning(BigDecimal orderQuantity, BigDecimal monthlySales);

    List<AppTopDisOrderGoodsOut> findTopOrderDetailListByOrderId(Long orderId, Integer topNum);

    OrdDisOrderDetail getOrderDetailByIdAndOrderId(Long id, Long orderId);

    @Transactional(rollbackFor = Exception.class)
    void batchSaveDisOrderDetail(List<OrdDisOrderDetail> ordDisOrderDetailList);
}
