package com.edc.erp.directly.returnnoticeorder.service;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.model.in.*;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderDetailOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;



/**
 * 退货通知单表(OrdDirReturnNotice)表服务接口
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
public interface OrdDirReturnNoticeService extends BaseService<OrdDirReturnNotice> {

    /**
     * 分页查询退货通知单列表
     * @param ordReturnNoticeIn
     * @return
     */
    Page<OrdReturnNoticeOrderOut> findReturnNoticeOrderOutForPage(OrdReturnNoticeIn ordReturnNoticeIn);

    /**
     * 分页查询退货通知单详情
     * @param ordReturnNoticeIn
     * @return
     */
    OrdReturnNoticeOrderDetailOut findReturnNoticeOrderDetailOutForPage(OrdReturnNoticeDetailIn ordReturnNoticeIn);

    /**
     * 保存或修改退货通知单及明细
     * @param saveOrdReturnNotice
     * @return
     */
    Response saveOrUpdateOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice);

    /**
     * 审核退货通知单
     * @param saveOrdReturnNotice
     * @return
     */
    Response auditOrdDirReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice);


    /**
     * 作废退货通知单
     * @param id
     * @return
     */
    Response invalidatedOrdReturnNotice(Integer id);

    /**
     * 导入商品信息
     * @param fileId
     * @param bizOrgCode
     * @return
     */
    Response<List<OrdReturnNoticeGoodsOut>> importReturnNoticeGoods(String fileId,List<String> goodsCodes, String bizOrgCode);

    /**
     * 导入门店信息
     * @param fileId
     * @param bizOrgCode
     * @param returnType
     * @return
     */
    Response<List<OrdReturnNoticeStoreOut>> importReturnNoticeStore(String fileId, String bizOrgCode, String returnType);

    /**
     * 根据id查退货通知单
     * @param returnNoticeOrderId
     * @return
     */
    OrdDirReturnNotice getReturnNoticeOrderById(Integer returnNoticeOrderId);

    /**
     * 根据通知单主键和商品门店代码查最大退货数
     * @param goodsCode
     * @param storeCode
     * @param id
     * @return
     */
    BigDecimal getMaxQtyByParameter(String goodsCode, String storeCode, Integer id);

    /**
     * 根据单号校验退货通知单是否存在以及是否已生效状态
     * @param returnNoticeOrderNo
     * @param bizOrgCode
     * @return
     */
    Response checkReturnNoticeNo(String returnNoticeOrderNo, String bizOrgCode);

    /**
     * 根据退货通知单查询可退商品信息
     * @param ordReturnNoticeIn
     * @return
     */
    OrdReturnNoticeOrderDetailOut getReturnNoticeOrderDetailById(OrdReturnNoticeDetailIn ordReturnNoticeIn);

    boolean isOvertimeForSubmit(Integer returnNoticeOrderId);

    /**
     * 导入明细信息
     * @param importNoticeDetailIn
     */
    Response<String> importDetail(ImportNoticeDetailIn importNoticeDetailIn);

    /**
     * 导出明细信息
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
