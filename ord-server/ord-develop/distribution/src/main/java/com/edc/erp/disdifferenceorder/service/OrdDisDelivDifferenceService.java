package com.edc.erp.disdifferenceorder.service;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.in.ApprovedDisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.DisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disdifferenceorder.model.out.DisDiffOrderSummaryOut;
import com.edc.erp.disdifferenceorder.model.out.DisDifferenceOrderOut;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.DifferenceBillDtlVO;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;



/**
 * 配销差异单(OrdDisDelivDifference)表服务接口
 *
 * @author weichao
 * @since 2022-10-24 11:16:25
 */
public interface OrdDisDelivDifferenceService extends BaseService<OrdDisDelivDifference> {
    /**
     * 获取差异单列表
     * @param differenceOrderIn
     * @return
     */
    Page<DisDifferenceOrderOut> findDiffOrderByParam(DisDifferenceOrderIn differenceOrderIn);

    /**
     * 获取差异单单头信息
     * @param differenceOrderId
     * @return
     */
    DisDifferenceOrderOut getBackHeaderDifferenceOrderOutById(Integer differenceOrderId);

    /**
     * 导出配销差异单列表出参
     * @param differenceOrderIn
     * @return
     */
    String exportDisDifferences(DisDifferenceOrderIn differenceOrderIn);

    /**
     * 作废配销差异单
     * @param ordDisDelivDifference
     * @return
     */
    int invalidDisDifference(OrdDisDelivDifference ordDisDelivDifference, StockInfoOut stockInfoOut);

    /**
     * 修改配销差异单
     * @param ordDisDelivDifference
     * @param typeCode
     * @param typeName
     * @param name
     * @return
     */
    int updateDisDifference(OrdDisDelivDifference ordDisDelivDifference,String typeCode,String typeName, String name);

    /**
     * 批量批准配销差异单
     * @param disDifferenceOrderIds
     * @return
     */
    int batchApprovedDisDifference(List<Integer> disDifferenceOrderIds);

    /**
     * 批准配销差异单
     * @param approvedDisDifferenceOrderIn
     * @return
     */
    int approvedDisDifference(ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn);

    /**
     * 冲销配销差异单
     * @param approvedDisDifferenceOrderIn
     * @return
     */
    int chargeDisDifference(ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn);

    /**
     * 新增配销差异单
     *
     * @param ordDisDelivDifference
     * @return
     */
    int insertOrdDisDelivDifference(OrdDisDelivDifference ordDisDelivDifference);

    /**
     * 校验配销差异单
     * @param saveDifferenceIn
     * @return
     */

    /**
     * 查询门店库存
     * @param bizOrgCode
     * @param goodsCode
     * @param storeCode
     * @return
     */
    BigDecimal checkStoreInv(String bizOrgCode, String goodsCode, String storeCode);

    /**
     * 查询仓储库存
     * @param bizOrgCode
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal checkStockInv(String bizOrgCode, String goodsCode, String stockCode);

    /**
     * 保存配销差异单
     * @param saveDifferenceIn
     * @return
     */
    Response saveDisDifference(SaveDifferenceIn saveDifferenceIn);



    /**
     * 释放库存,资金
     * @param ordDisDelivDifference
     * @param ordDisDelivDifferenceDetails
     */
    void releaseStockAndFund(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails, StockInfoOut stockInfoOut, LocalDateTime flowDate);

    /**
     * 初始化差异单（dts）
     * @param ordDisDelivDifference
     * @param detail
     * @return
     */
    List<OrdDisDelivDifferenceDetail> initDifferenceBillVO(OrdDisDelivDifference ordDisDelivDifference, List<DifferenceBillDtlVO> detail, String centerStockBizOrgCode);

    /**
     * 查询配销差异单列表
     * @param disDelivDifference
     * @return
     */
    List<DisDifferenceOrderOut> findDisDifferenceOrders(OrdDisDelivDifference disDelivDifference);

    /**
     * 配销差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO);

    /**
     * 通过配销单号查询差异单号
     * @param deliveryOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDifferenceNoByDeliveryNo(String deliveryOrderNo, String bizOrgCode);

    /**
     * 通过差异单号查询配销单号
     * @param differenceOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDeliveryNoByDiffNo(String differenceOrderNo, String bizOrgCode);

    /**
     * 加盟差异单汇总
     * @param differenceOrderIn
     * @return
     */
    DisDiffOrderSummaryOut diffOrderSummary(DisDifferenceOrderIn differenceOrderIn);

    OrdDisDelivDifference getOneByOrderNo(String differenceNo);
}
