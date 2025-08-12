package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.directly.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.directly.distribution.model.in.SaveDirDistributionDetailIn;
import com.edc.erp.directly.distribution.model.out.DirDistributionCheckOut;
import com.edc.erp.directly.distribution.model.out.OrdDirDistributionImportResultOut;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
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
 * 直营分货门店商品关联表(OrdDirOrderDistributionDetail)表服务接口
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
public interface OrdDirOrderDistributionDetailService extends BaseService<OrdDirOrderDistributionDetail> {

    /**
     * 修改直营分货门店商品
     * @param ordDisOrderDistributionDetail
     * @return
     */
    int updateOrdDirOrderDistributionDetail(OrdDirOrderDistributionDetail ordDisOrderDistributionDetail);

    /**
     * 删除直营分货门店商品
     * @param deleteStoreGoodsIn 删除门店商品关联入参类
     * @return
     */
    int deleteOrdDirOrderDistributionDetail(DeleteStoreGoodsIn deleteStoreGoodsIn);

    /**
     * 查询直营分货门店商品
     * @param distributionOrderId
     * @param goodsCode
     * @param storeCode
     * @return
     */
    List<OrdDirOrderDistributionDetail> findOrdDirOrderDistributions(Long distributionOrderId, String goodsCode, String storeCode);

    /**
     * 获取分货单
     * @param distributionOrderId
     * @param goodsCode
     * @param storeCode
     * @return
     */
    OrdDirOrderDistributionDetail getOrdDirOrderDistribution(Long distributionOrderId, String goodsCode, String storeCode);

    /**
     * 统计分货总数量
     * @param distributionOrderId 直营分货单主键
     * @return
     */
    BigDecimal sumTotalDistributionQuantity(Long distributionOrderId);

    /**
     * 分页查询直营分货门店商品
     * @param queryOrderDistributionDetailIn 直营分货门店商品入参类
     * @return
     */
    Page<OrdDirOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 根据分货单id查询当前商品门店明细
     * @param ordDistributionOrderId 分货单id
     * @return
     */
    List<OrdDirOrderDistributionDetail> getDetailByOrdDistributionOrderId(Long ordDistributionOrderId);

    /**
     * 保存分货单明细
     * @param distributionOrder 分货单
     * @param ordDisOrderDistributionDetails 分货单明细
     * @param isUpdateOrder 是否更新分货单 0否 1是
     * @return
     */
    void save(OrdDirOrderDistribution distributionOrder, List<OrdDirOrderDistributionDetail> ordDisOrderDistributionDetails, int isUpdateOrder);

    /**
     * 获取分货明细
     * @param ordDistributionOrderId 直营分货单
     * @param goodsCode 商品code
     * @param storeCode 门店代码
     * @return
     */
    OrdDirOrderDistributionDetail getOrdDisOrderDistribution(Long ordDistributionOrderId, String goodsCode, String storeCode);

    void batchSaveDetail(List<OrdDirOrderDistributionDetail> details);

    DirDistributionCheckOut handleCheckBeforeSaveDistributionDetail(Map<String, OrdDirDistributionImportResultOut> importResultMap,
                                                                    OrdDirOrderDistribution ordDirOrderDistribution, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    void saveImportDistributionDetail(DirDistributionCheckOut dirDistributionCheckOut);

    StringJoiner checkImportDistributionGoods(OrderGoodsOut goodsOut, Long detailId, Map<String, OrdDirOrderDistributionDetail> existDetailMap);

    OrdDirDistributionImportErrorResult initOrdDirDistributionImportErrorResult(String storeCode, String goodsCode,
                                                                                BigDecimal distributionQuantity, String errorMessage);

    StringJoiner checkAuditDistributionGoods(OrderGoodsOut goodsOut, LocalDateTime targetDateTime);

    void batchUpdateDistributionQuantity(List<OrdDirOrderDistributionDetail> detailList);

    @Transactional(rollbackFor = Exception.class)
    void updateAfterAuditSuccess(List<OrdDirOrderDistributionDetail> updateDetailList, OrdDirOrderDistribution ordDirOrderDistribution);

    Response<DirDistributionCheckOut> checkForSaveDetail(SaveDirDistributionDetailIn saveDirDistributionDetailIn, String loginUsername);

    List<OrdDirDistributionImportErrorResult> checkDetailByAudit(List<OrdDirOrderDistributionDetail> detailList, String bizOrgCode, LocalDateTime effectiveTime);

    int countByDistributionOrderId(Long distributionOrderId);

    void deleteDetailById(Long id, OrdDirOrderDistribution orderDistribution);
}
