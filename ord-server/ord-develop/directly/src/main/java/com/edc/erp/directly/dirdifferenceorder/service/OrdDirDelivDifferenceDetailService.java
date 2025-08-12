package com.edc.erp.directly.dirdifferenceorder.service;

import com.edc.erp.directly.dirdeliveryorder.model.out.DirDeliveryOrderDetailsOut;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.model.in.OrdDirDelivDifferenceDetailIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.OrdDirDelivDifferenceDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;



/**
 * 差异单详细表(OrdDirDelivDifferenceDetail)表服务接口
 *
 * @author weichao
 * @since 2022-11-14 11:32:46
 */
public interface OrdDirDelivDifferenceDetailService extends BaseService<OrdDirDelivDifferenceDetail> {
    /**
     *品项数
     * @param id
     * @return
     */
    Integer getGoodsSize(Integer id);

    /**
     * 获取直营配货差异单详情
     * @param dirDifferenceDetailIn
     * @return
     */
    Page<OrdDirDelivDifferenceDetailOut>  findDifferenceOrderDtlListByParameter(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn);

    /**
     * 批量新增差异单明细
     * @param details
     */
    void batchSave(List<OrdDirDelivDifferenceDetail> details);

    /**
     * 修改直营配货差异单明细
     * @param differenceDetail
     */
    void updateDirDifferenceDetail(OrdDirDelivDifferenceDetail differenceDetail);

    /**
     * 导出配货差异单详情
     * @param dirDifferenceDetailIn
     * @return
     */
    String exportDirDifferenceDetails(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn);

    /**
     * 批量修改直营配货差异单明细
     * @param ordDirDelivDifferenceDetails
     */
    void batchUpdate(List<OrdDirDelivDifferenceDetail> ordDirDelivDifferenceDetails);

    /**
     * 根据差异单主键查询差异单明细
     * @param id
     * @return
     */
    List<OrdDirDelivDifferenceDetailOut> findDifferenceOrderDtlById(Integer id);
}
