package com.edc.erp.directly.dirfirstorder.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.OrdDirFirstOrderImportErrorResult;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirDeleteFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.SaveDirFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.out.DirFirstOrderCheckOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 铺货单明细表(OrdDirOrderFirstDetail)表服务接口
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
public interface OrdDirOrderFirstDetailService extends BaseService<OrdDirOrderFirstDetail> {
    /**
     * 查询铺货单详情
     * @param ordDirOrderFirstDetailIn
     * @return
     */
    List<OrdDirOrderFirstDetailOut> findOrdDirOrderFirstDetail(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn);

    /**
     * 批量保存铺货单明细
     * @param insertList
     * @param userName
     * @param finalFirstOrderId
     */
    void batchInsertFirstOrderDetail(List<OrdDirOrderFirstDetail> insertList, String userName, Long finalFirstOrderId);

    /**
     * 导入铺货单明细
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    Response<List<OrdDirOrderFirstDetailOut>> importFirstOrderDetail(String fileId, String storeCode, List<String> goodsCodes);

    /**
     * 导出铺货单明细列表
     * @param ordDirOrderFirstDetailIn
     * @return
     */
    String exportFirstOrderDetail(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn, List<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetails);

    /**
     * 根据主键删除铺货单明细
     * @param firstOrderId
     */
    void deleteByFirstOrderId(Long firstOrderId);

    DirFirstOrderCheckOut handleCheckBeforeSaveFirstOrderDetail(OrdDirOrderFirst ordDirOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername);

    void saveImportFirstOrderDetail(DirFirstOrderCheckOut dirFirstOrderCheckOut);

    int countByFirstOrderId(Long firstOrderId);

    List<OrdDirOrderFirstDetail> findAllByFirstOrderId(Long firstOrderId);

    List<OrdDirFirstOrderImportErrorResult> checkDetailByAudit(String storeCode, String bizOrgCode, List<OrdDirOrderFirstDetail> detailList, Map<String, OrderGoodsOut> checkGoodsMap, Map<String, Integer> importGoodsNumMap);

    @Transactional(rollbackFor = Exception.class)
    void updateAfterAuditSuccess(List<OrdDirOrderFirstDetail> updateDetailList, OrdDirOrderFirst ordDirOrderFirst);

    Response<DirFirstOrderCheckOut> checkFirstForSaveDetail(SaveDirFirstDetailIn saveDirFirstDetailIn, String loginUsername);

    Response<String> deleteDetail(OrdDirDeleteFirstOrderDetailIn ordDirDeleteFirstOrderDetailIn);
}
