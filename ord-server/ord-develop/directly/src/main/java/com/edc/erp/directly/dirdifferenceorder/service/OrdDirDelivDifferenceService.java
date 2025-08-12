package com.edc.erp.directly.dirdifferenceorder.service;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.model.in.ApprovedDirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.DirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDiffOrderSummaryOut;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDifferenceOrderOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.DifferenceBillDtlVO;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



/**
 * 差异单(OrdDirDelivDifference)表服务接口
 *
 * @author weichao
 * @since 2022-11-14 11:32:31
 */
public interface OrdDirDelivDifferenceService extends BaseService<OrdDirDelivDifference> {
    /**
     * 分页获取差异单列表
     * @param differenceOrderIn
     * @return
     */
    Page<DirDifferenceOrderOut> findDiffOrderByParam(DirDifferenceOrderIn differenceOrderIn);

    /**
     * 获取差异单表头信息
     * @param differenceOrderId
     * @return
     */
    DirDifferenceOrderOut getBackHeaderDifferenceOrderOutById(Integer differenceOrderId);

    /**
     * 保存差异单
     * @param saveDifferenceIn
     * @return
     */
    Response saveDirDifference(SaveDifferenceIn saveDifferenceIn);

    /**
     * 查询可用仓储库存
     * @param bizOrgCode
     * @param goodsCode
     * @param stockCode
     * @return
     */
    BigDecimal checkStockInv(String bizOrgCode, String goodsCode, String stockCode);

    /**
     * 查询门店可用库存
     * @param bizOrgCode
     * @param goodsCode
     * @param storeCode
     * @return
     */
    BigDecimal checkStoreInv(String bizOrgCode, String goodsCode, String storeCode);

    /**
     * 批准差异单
     * @param approvedDirDifferenceOrderIn
     * @return
     */
    int approvedDisDifference(ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn);

    /**
     * 修改差异单
     * @param ordDirDelivDifference
     * @param typeCode
     * @param typeName
     * @param name
     * @return
     */
    int updateDirDifference(OrdDirDelivDifference ordDirDelivDifference,String typeCode,String typeName, String name);

    /**
     * 作废差异单
     * @param dirDelivDifference
     * @return
     */
    int invalidDirDifference(OrdDirDelivDifference dirDelivDifference, StockInfoOut stockInfoOut);

    /**
     * 冲销差异单
     * @param approvedDirDifferenceOrderIn
     * @return
     */
    int chargeDirDifference(ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn);

    /**
     * 导出差异单列表
     * @param differenceOrderIn
     * @return
     */
    String exportDirDifferences(DirDifferenceOrderIn differenceOrderIn);

    /**
     * 初始化差异单（dts）
     * @param ordDirDelivDifference
     * @param detail
     * @return
     */
    List<OrdDirDelivDifferenceDetail> initDifferenceBillVO(OrdDirDelivDifference ordDirDelivDifference, List<DifferenceBillDtlVO> detail, String centerStockBizOrgCode);

    /**
     * 释放库存
     * @param ordDirDelivDifference
     * @param ordDirDelivDifferenceDetails
     */
    void releaseStock(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> ordDirDelivDifferenceDetails, StockInfoOut stockInfoOut, LocalDateTime flowDate);


    /**
     * 查询配货差异单列表（库存盘点）
     * @param dirDelivDifference
     * @return
     */
    List<DirDifferenceOrderOut> findDirDifferenceOrders(OrdDirDelivDifference dirDelivDifference);

    /**
     * 批量批准配货差异单
     * @param dirDifferenceOrderIds
     * @return
     */
    int batchApprovedDirDifference(List<Integer> dirDifferenceOrderIds);

    /**
     * 差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO);

    /**
     * 通过配货单号查询差异单号
     * @param deliveryOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDifferenceNoByDeliveryNo(String deliveryOrderNo, String bizOrgCode);

    /**
     * 通过差异单号查询配货单号
     * @param differenceOrderNo
     * @param bizOrgCode
     * @return
     */
    String getDeliveryNoByDiffNo(String differenceOrderNo, String bizOrgCode);

    /**
     * 直营差异单汇总
     * @param differenceOrderIn
     * @return
     */
    DirDiffOrderSummaryOut diffOrderSummary(DirDifferenceOrderIn differenceOrderIn);

    OrdDirDelivDifference getOneByOrderNo(String differenceNo);
}
