package com.edc.erp.disfirstorder.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import com.edc.erp.disfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.disfirstorder.model.excel.OrdDisFirstOrderImportErrorResult;
import com.edc.erp.disfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisDeleteFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstDetailIn;
import com.edc.erp.disfirstorder.model.in.SaveDisFirstDetailIn;
import com.edc.erp.disfirstorder.model.out.DisFirstOrderCheckOut;
import com.edc.erp.disfirstorder.model.out.OrdDisOrderFirstDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 配销铺货单明细表(OrdDisOrderFirstDetail)表服务接口
 *
 * @author weichao
 * @since 2022-10-10 16:18:10
 */
public interface OrdDisOrderFirstDetailService extends BaseService<OrdDisOrderFirstDetail> {
    /**
     * 获取配销铺货单明细列表
     * @param ordDisOrderFirstDetailIn
     * @param bizOrgCode
     * @return
     */
    Page<OrdDisOrderFirstDetailOut> findOrdDisOrderFirstDetailPage(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn, String bizOrgCode);

    /**
     * 校验铺货单商品
     * @param insertFirstOrderDetailIn
     * @param bizOrgCode
     * @return
     */
    Response checkRepeatParam(InsertFirstOrderDetailIn insertFirstOrderDetailIn, String bizOrgCode);

    /**
     * 新增铺货单明细
     * @param insertFirstOrderDetailIn
     * @return
     */
    Response<OrdDisOrderFirst> saveFirstOrderDetail(InsertFirstOrderDetailIn insertFirstOrderDetailIn);

    /**
     * 导出铺货单明细
     * @param ordDisOrderFirstDetailIn
     * @return
     */
    String exportFirstOrderDetail(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn);

    /**
     * 导入铺货单明细
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    Response<List<OrdDisOrderFirstDetailOut>> importFirstOrderDetail(String fileId, String storeCode, List<String> goodsCodes);

    List<OrdDisOrderFirstDetail> findDetailListByFirstOrderId(Long firstOrderId);

    List<OrdDisOrderFirstDetail> findAllByFirstOrderId(Long firstOrderId);

    List<OrdDisFirstOrderImportErrorResult> checkDetailByAudit(String storeCode, String bizOrgCode, List<OrdDisOrderFirstDetail> detailList,
                                                               Map<String, OrderGoodsOut> checkGoodsMap, Map<String, Integer> importGoodsNumMap);

    int countByFirstOrderId(Long firstOrderId);

    @Transactional(rollbackFor = Exception.class)
    void updateAfterAuditSuccess(List<OrdDisOrderFirstDetail> updateDetailList, OrdDisOrderFirst ordDirOrderFirst);

    DisFirstOrderCheckOut handleCheckBeforeSaveFirstOrderDetail(OrdDisOrderFirst ordDirOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    void saveImportFirstOrderDetail(DisFirstOrderCheckOut disFirstOrderCheckOut);

    Response<DisFirstOrderCheckOut> checkFirstForSaveDetail(SaveDisFirstDetailIn saveDisFirstDetailIn, String loginUsername);

    Response<String> deleteDetail(OrdDisDeleteFirstOrderDetailIn ordDisDeleteFirstOrderDetailIn);
}
