package com.edc.erp.wholesale.returns.service;

import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnDetailFilterIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsDetailIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDetailAndGoodsStrOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 批发退货明细单(WholesaleReturnDetail)表服务接口
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
public interface WholesaleReturnDetailService extends BaseService<WholesaleReturnDetail> {

    /**
     * 根据条件查询批发退货详情
     *
     * @param wholesaleReturnDetail
     * @return
     */
    List<WholesaleReturnDetail> findWholesaleReturnDetail(WholesaleReturnDetail wholesaleReturnDetail);

    /**
     * 批量保存批发退货单详情
     *
     * @param wholesaleReturnDetailList
     */
    void insertWholesaleReturnDetailList(List<WholesaleReturnDetail> wholesaleReturnDetailList);

    /**
     * 更新批发退货单详情
     *
     * @param wholesaleReturnDetail
     * @return
     */
    Integer update(WholesaleReturnDetail wholesaleReturnDetail);

    /**
     * 审核批发退货单详情
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    Response auditWholesaleReturn(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);

    /**
     * 作废批发退货单详情
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    Response invalidWholesaleReturnDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);


    /**
     * 删除批发退货详情数据
     *
     * @param wholesaleReturnDetail
     * @return
     */
    Integer delete(WholesaleReturnDetail wholesaleReturnDetail);

    /**
     * 根据商品代码或者商品名称筛查订单物品
     *
     * @param wholesaleReturnDetailFilterIn
     * @return
     */
    Response<WholesaleReturnAndDetailOut> filterWholesaleReturnDetail(WholesaleReturnDetailFilterIn wholesaleReturnDetailFilterIn);

    /**
     * 冲销批发退货单详情
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    Response writeOffWholesaleReturnDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);

    /**
     * 导入批发退货单明细
     *
     * @param fileId
     * @param clientCode
     * @param stockCode
     * @param warehouseCode
     * @param stockId
     * @return
     */
    Response<List<WholesaleReturnDetailAndGoodsStrOut>> importWholesaleReturnDetail(String fileId, String clientCode, String stockCode, String warehouseCode, Integer stockId);

    /**
     * 导出批发退货单明细
     *
     * @param wholesaleReturnsId
     * @return
     */
    String exportWholesaleReturnDetail(Long wholesaleReturnsId);

    /**
     * 批发退货单资管调整参数转换
     *
     * @param wholesaleReturns
     * @param newWholesaleReturnNo
     * @param returnStatus
     * @return
     */
    RechargeLiquidationIn chargeParamToRechargeLiquidationIn(WholesaleReturns wholesaleReturns, String newWholesaleReturnNo, String returnStatus);

    /**
     * 批发退货单库存调整参数转换
     *
     * @param wholesaleReturnsDetailIn
     * @param returnStatus
     * @return
     */
    List<StockFlowIn> addOrSubStock(WholesaleReturnsDetailIn wholesaleReturnsDetailIn, String returnStatus);

    /**
     * 根据批发出货单主键生成批发退货单数据
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    Response<WholesaleReturnAndDetailOut> outToReturn(Long id, String bizOrgCode);

    /**
     * 批量更新批发退货单明细
     * @param wholesaleReturnDetailList 批发退货单明细
     */
    void batchUpdate(List<WholesaleReturnDetail> wholesaleReturnDetailList);

//    Response<String> batchAuditWholesaleReturn(List<Long> wholesaleReturnsDetailInList);

    List<WholesaleReturnDetail> findListByWholesaleReturnId(Long wholesaleReturnId);

    WholesaleReturnDateInfoOut sumWholesaleReturnDateInfoByIdList(List<Long> idList);

    @Transactional(rollbackFor = Exception.class)
    void batchUpdateForAudit(List<WholesaleReturnDetail> wholesaleReturnDetailList);
}
