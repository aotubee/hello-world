package com.edc.erp.returnnoticeorder.service;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.model.in.*;
import com.edc.erp.returnnoticeorder.model.out.OrdBackHeaderReturnNoticeOrderOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeOrderOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;


/**
 * 退货通知单表(DisReturnNotice)表服务接口
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:34
 */
public interface OrdDisReturnNoticeService extends BaseService<OrdDisReturnNotice> {

    /**
     * 新增退货通知单
     * @param ordReturnNotice
     * @return
     */
    int insertReturnNotice(OrdDisReturnNotice ordReturnNotice);

    /**
     * 分页查询退货通知单列表
     *
     * @param ordReturnNoticeIn 退货通知单入参
     * @return
     */
    Page<OrdReturnNoticeOrderOut> findReturnNoticeOrderOutForPage(OrdReturnNoticeIn ordReturnNoticeIn);


    /**
     * 根据主键查找一个退货通知单
     *
     * @param returnNoticeOrderId 退货通知单主键
     * @return
     */
    OrdDisReturnNotice getReturnNoticeOrderById(Integer returnNoticeOrderId);

    /**
     * 作废退货通知单
     *
     * @param ordDisReturnNotice
     * @return
     */
    int invalidatedOrdReturnNotice(OrdDisReturnNotice ordDisReturnNotice);

    /**
     * 审核退货通知单
     *
     * @param saveOrdReturnNotice
     * @return
     */
    Response auditOrdDisReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice);

    /**
     * 保存退货通知单明细
     *
     * @param saveOrdReturnNotice
     * @param
     * @return
     */
    Response saveOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice);


    /**
     * 查询退货通知单详情
     * @param ordReturnNoticeIn
     * @return
     */
    OrdBackHeaderReturnNoticeOrderOut findReturnNoticeOrderDetailOutForPage(OrdReturnNoticeDetailIn ordReturnNoticeIn);

    /**
     * 根据单号校验退货通知单是否存在以及是否已生效状态
     * @param returnNoticeOrderNo
     * @param bizOrgCode
     * @return
     */
    Response checkReturnNoticeNo(String returnNoticeOrderNo, String bizOrgCode);

    /**
     * 根据退货通知单查询可退商品信息(App)
     * @param ordReturnNoticeIn
     * @return
     */
    OrdBackHeaderReturnNoticeOrderOut getReturnNoticeOrderDetailById(OrdReturnNoticeDetailIn ordReturnNoticeIn);

    boolean isOvertimeForSubmit(Integer returnNoticeOrderId);

    /**
     * 导入明细
     * @param importNoticeDetailIn
     * @return
     */
    Response<String> importDetail(ImportNoticeDetailIn importNoticeDetailIn);

    /**
     * 导出明细
     * @param noticeOrderId
     * @param bizOrgCode
     * @return
     */
    String export(Integer noticeOrderId, String bizOrgCode);

    /**
     * 查询未退生效通知单的数量
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    int getUnreturnedCount(@Param("storeCode") String storeCode, @Param("bizOrgCode") String bizOrgCode);
}
