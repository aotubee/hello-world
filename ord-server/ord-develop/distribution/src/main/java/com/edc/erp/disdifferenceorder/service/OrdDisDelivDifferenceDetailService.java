package com.edc.erp.disdifferenceorder.service;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * 配销差异单详细表(OrdDisDelivDifferenceDetail)表服务接口
 *
 * @author weichao
 * @since 2022-10-24 11:16:38
 */
public interface OrdDisDelivDifferenceDetailService extends BaseService<OrdDisDelivDifferenceDetail> {
    /**
     * 获取差异单详情
     * @param disDifferenceDetailIn
     * @return
     */
    Page<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlListByParameter(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn);

    /**
     * 导出配销差异单详情
     * @param disDifferenceDetailIn
     * @return
     */
    String exportDisDifferenceDetails(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn);

    /**
     * 修改配销差异单明细
     * @param differenceDetail
     */
    void updateDisDifferenceDetail(OrdDisDelivDifferenceDetail differenceDetail);

    /**
     * 批量新增差异单明细
     * @param delivDifferenceDetails
     */
    void batchSave(List<OrdDisDelivDifferenceDetail> delivDifferenceDetails);

    /**
     * 批量修改差异单明细
     * @param ordDisDelivDifferenceDetails
     */
    void batchUpdate(List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails);

    /**
     * 查询商品品项数
     * @param id
     * @return
     */
    Integer getGoodsSize(Integer id);

    /**
     * 根据差异单主键查询差异单详情
     * @param differenceId
     * @return
     */
    List<OrdDisDelivDifferenceDetail> selectAllByDifferenceOrderId(Integer differenceId);

    /**
     * 根据差异单主键查询差异单详情
     * @param id
     * @return
     */
    List<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlById(Integer id);

}
