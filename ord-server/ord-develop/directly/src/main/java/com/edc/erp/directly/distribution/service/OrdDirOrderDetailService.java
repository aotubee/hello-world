package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.model.in.warning.LogisticsMessageOrderQuantityIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.model.in.OrderDetailIn;
import com.edc.erp.directly.distribution.model.out.AppTopDirOrderGoodsOut;
import com.edc.erp.directly.distribution.model.out.BackHeaderOrderDetailOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailOut;
import com.edc.erp.directly.distribution.model.out.StoreOrderGoodsOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订货单详细表(OrdDirOrderDetail)表服务接口
 *
 * @author wanglidong
 * @since 2022-11-15 18:09:28
 */
public interface OrdDirOrderDetailService extends BaseService<OrdDirOrderDetail> {

    /**
     * 根据订单id查询明细信息
     *
     * @param orderId
     * @return
     */
    List<OrdDirOrderDetail> findOrderDetailListByOrderId(Long orderId);

    /**
     * 导出直营订货单详情
     *
     * @param orderDetailIn
     * @return
     */
    String exportOrdDirOrderDetail(OrderDetailIn orderDetailIn);

    /**
     * 直营订货单明细列表查询
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    Page<OrderDetailOut> findOrderDetailOutPage(OrderDetailIn orderDetailIn, String bizOrgCode);

    /**
     * 删除存在的sku信息
     *
     * @param ordDirOrder
     */
    void removeSkuForExistsQuantity(OrdDirOrder ordDirOrder);

    /**
     * 运营端作废直营订货单
     *
     * @param ordDirOrder
     * @param userName
     * @param bizOrgCode
     */
    void invalidOrder(OrdDirOrder ordDirOrder, String userName, String bizOrgCode);

    /**
     * 加推直营订货单
     *
     * @param ordDirOrder
     * @param userName
     * @return
     */
    Response addPushOrder(OrdDirOrder ordDirOrder, String userName);

    /**
     * 保存订货单详情
     *
     * @param orderDetail
     */
    void save(OrdDirOrderDetail orderDetail);

    /**
     * 查询直营订货单明细表头
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    BackHeaderOrderDetailOut getBackHeaderOrderDetailOut(Long orderId, String bizOrgCode);

    BigDecimal sumPackageByOrderIdList(String skuCode, List<Long> orderIdList, AppUserOut appUserOut, String truncationTimeStr);

    /**
     * 校验订货量
     *
     * @param orderQuantity 本次订货总数
     * @param monthlySales  月销量
     * @return
     */
    int getIsNeedOrderQuantityWarning(BigDecimal orderQuantity, BigDecimal monthlySales);

    List<AppTopDirOrderGoodsOut> findTopOrderDetailListByOrderId(Long orderId, Integer topNum);

    OrdDirOrderDetail getOrderDetailByIdAndOrderId(Long id, Long orderId);

    BigDecimal sumOrderSkuQuantity(LogisticsMessageOrderQuantityIn logisticsMessageOrderQuantityIn);

    void batchSaveDirOrderDetail(List<OrdDirOrderDetail> ordDirOrderDetailList);

    int countByOrderId(Long id);

    Map<String, BigDecimal> findStoreOrderQuantityByTruncationTime(LocalDateTime truncationTime, String storeCode);
}
