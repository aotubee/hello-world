package com.edc.erp.wholesale.returns.service;

import com.edc.erp.wholesale.model.in.returns.WholesaleRetReceivingIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsDetailIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsListIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnsListOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import com.edc.sdk.dts.model.order.vo.WholesaleReBillVO;

import java.util.List;
import java.util.Map;


/**
 * 批发退货单(WholesaleReturns)表服务接口
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
public interface WholesaleReturnsService extends BaseService<WholesaleReturns> {

    /**
     * 批发退货单列表查询
     *
     * @param wholesaleReturnsListIn
     * @return
     */
    List<WholesaleReturnsListOut> findWholesaleReturnsForPage(WholesaleReturnsListIn wholesaleReturnsListIn);

    /**
     * 根据批发退货单单号查询退货单详情
     *
     * @param wholesaleReturnNo
     * @return
     */
    WholesaleReturnAndDetailOut getDetailByWholesaleReturnNo(String wholesaleReturnNo);

    /**
     * 保存批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    Response saveWholesaleReturnsDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);

    /**
     * 校验订单是否允许被创建或审核通过
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    Response checkWholesaleReturn(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);

    /**
     * 生成批发退货单单号
     *
     * @return
     */
    String createWholesaleReturnNo();

    /**
     * 统计申请数量、申请金额、入库数量及实际入库金额，并更新进批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    WholesaleReturnsDetailIn countParam(WholesaleReturnsDetailIn wholesaleReturnsDetailIn);

    /**
     * 批发退-手动收货
     * @param wholesaleRetReceivingIn 批发退-手动收货 入参
     * @return
     */
    Response<String> wholesaleReturnsReceiving(WholesaleRetReceivingIn wholesaleRetReceivingIn);

    /**
     * 批发退货单回传
     *
     * @param wholesaleReBillVO
     * @return
     */
    boolean wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO);

    /**
     * 根据退货单关联的出货单单号查询出货单
     * @param wholesaleShipmentNo 出货单单号
     * @param bizOrgCode 业务组织
     * @return
     */
    Map<String, WholesaleShipmentDetailIn> getShipmentByWholesaleShipmentNo(String wholesaleShipmentNo, String bizOrgCode);

    WholesaleReturns getOneById(Long id);

    int countOneBySourceNo(String sourceNo, String bizOrgCode);

    WholesaleReturns getOneByWholesaleReturnNo(String wholesaleReturnNo, String bizOrgCode);

    String exportWholesaleReturnOrder(WholesaleReturnsListIn wholesaleReturnsListIn);

    WholesaleReturnDateInfoOut sumWholesaleReturnDateInfo(WholesaleReturnsListIn wholesaleReturnsListIn);

    WholesaleReturns getOneByOrderNo(String orderNo);

    boolean checkIsHsReturn(String sourceNo);

}
