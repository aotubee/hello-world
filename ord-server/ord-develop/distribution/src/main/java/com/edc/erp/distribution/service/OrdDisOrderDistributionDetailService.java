package com.edc.erp.distribution.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.distribution.model.in.SaveDisDistributionDetailIn;
import com.edc.erp.distribution.model.out.DisDistributionCheckOut;
import com.edc.erp.distribution.model.out.OrdDisDistributionImportResultOut;
import com.edc.erp.distribution.model.out.OrdDisOrderDistributionDetailOut;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 * 配销分货门店商品关联表(OrdDisOrderDistributionDetail)表服务接口
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
public interface OrdDisOrderDistributionDetailService extends BaseService<OrdDisOrderDistributionDetail> {

    /**
     * 修改配销分货门店商品
     * @param ordDisOrderDistributionDetail
     * @return
     */
    int updateOrdDisOrderDistributionDetail(OrdDisOrderDistributionDetail ordDisOrderDistributionDetail);

    /**
     * 删除配销分货门店商品
     * @param deleteStoreGoodsIn 删除门店商品关联入参类
     * @return
     */
    int deleteOrdDisOrderDistributionDetail(DeleteStoreGoodsIn deleteStoreGoodsIn);

    /**
     * 查询配销分货门店商品
     * @param distributionOrderId
     * @param goodsCode
     * @param storeCode
     * @return
     */
    List<OrdDisOrderDistributionDetail> findOrdDisOrderDistributions(Long distributionOrderId, String goodsCode, String storeCode);

    @Transactional(rollbackFor = Exception.class)
    void batchSaveDetail(List<OrdDisOrderDistributionDetail> details);

    DisDistributionCheckOut handleCheckBeforeSaveDistributionDetail(Map<String, OrdDisDistributionImportResultOut> importResultMap,
                                                                    OrdDisOrderDistribution ordDirOrderDistribution, String loginUsername, String distributionIdentification);

    @Transactional(rollbackFor = Exception.class)
    void saveImportDistributionDetail(DisDistributionCheckOut disDistributionCheckOut);

    StringJoiner checkImportDistributionGoods(OrderGoodsOut goodsOut, Long detailId, Map<String, OrdDisOrderDistributionDetail> existDetailMap,
                                              OrdDisPresaleAssets presaleAssets, BigDecimal distributionQuantity);

    OrdDisDistributionImportErrorResult initOrdDisDistributionImportErrorResult(String storeCode, String goodsCode,
                                                                                BigDecimal distributionQuantity, String errorMessage);

    StringJoiner checkAuditDistributionGoods(OrderGoodsOut goodsOut, LocalDateTime effectiveTime, Boolean isAssetsFlag, Map<String, OrdDisPresaleAssetsDetail> storePresaleAssetsMap, BigDecimal distributionQuantity);

    @Transactional(rollbackFor = Exception.class)
    void batchUpdateDistributionQuantity(List<OrdDisOrderDistributionDetail> detailList);

    @Transactional(rollbackFor = Exception.class)
    void updateAfterAuditSuccess(List<OrdDisOrderDistributionDetail> updateDetailList, OrdDisOrderDistribution ordDisOrderDistribution);

    Response<DisDistributionCheckOut> checkForSaveDetail(SaveDisDistributionDetailIn saveDisDistributionDetailIn, String loginUsername, String distributionIdentification);

    List<OrdDisDistributionImportErrorResult> checkDetailByAudit(List<OrdDisOrderDistributionDetail> detailList, String bizOrgCode, LocalDateTime effectiveTime, String distributionIdentification);

    /**
     * 导出配销分货门店商品
     * @param distributionOrderId
     * @param goodsCode
     * @param storeCode
     * @return
     */
    String export(Long distributionOrderId, String goodsCode, String storeCode);

    /**
     * 获取分货明细
     * @param distributionOrderId 分货单id
     * @param goodsCode 商品code
     * @param storeCode 门店code
     * @return
     */
    OrdDisOrderDistributionDetail getOrdDisOrderDistribution(Long distributionOrderId, String goodsCode, String storeCode);

    /**
     * 统计分货总数量
     * @param distributionOrderId 配销分货单主键
     * @return
     */
    BigDecimal sumTotalDistributionQuantity(Long distributionOrderId);

    /**
     * 分页查询配销分货门店商品
     * @param queryOrderDistributionDetailIn 配销分货门店商品入参类
     * @return
     */
    Page<OrdDisOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 根据分货单id查询当前商品门店明细
     * @param ordDistributionOrderId 分货单id
     * @return
     */
    List<OrdDisOrderDistributionDetail> getDetailByOrdDistributionOrderId(Long ordDistributionOrderId);

    /**
     * 保存出货单明细
     * @param distributionOrder 出货单
     * @param ordDisOrderDistributionDetails 出货单明细
     * @param isUpdateOrder 是否更新分货单 0否 1是
     * @return
     */
    void save(OrdDisOrderDistribution distributionOrder, List<OrdDisOrderDistributionDetail> ordDisOrderDistributionDetails, int isUpdateOrder);

    int countByDistributionOrderId(Long distributionOrderId);

    void deleteDetailById(Long id, OrdDisOrderDistribution orderDistribution);
}
