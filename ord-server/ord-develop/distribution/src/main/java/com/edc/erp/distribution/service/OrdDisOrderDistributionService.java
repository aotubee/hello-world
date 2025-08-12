package com.edc.erp.distribution.service;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.*;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 配销分货单(OrdDisOrderDistribution)表服务接口
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:10
 */
public interface OrdDisOrderDistributionService extends BaseService<OrdDisOrderDistribution> {

    /**
     * 逻辑删除配销分货单
     * @param orderDistribution 分货单实体
     * @return
     */
    int logicDelOrdDisOrderDistribution(OrdDisOrderDistribution orderDistribution);

    /**
     * 修改配销分货单
     * @param orderDistribution 分货单实体
     * @return
     */
    int updateOrdDisOrderDistribution(OrdDisOrderDistribution orderDistribution);


    /**
     *  配销分货单表头
     * @param distributionOrderId  分货单主键
     * @return
     */
    BackHeaderOrdDistributionOrderOut getHeaderOrdDistributionOrderOutById(Long distributionOrderId);

    /**
     * 分页查询分货订单列表
     *
     * @param orderDistributionIn 分货单分页入参
     * @return
     */
    Page<OrdDisOrderDistributionOrderOut> findOrdDistributionOrder(OrdDisOrderDistributionIn orderDistributionIn);

    /**
     * 加载 OrdDisOrderDistribution对象
     * @param id
     * @return
     */
    OrdDisOrderDistribution getOrdDisOrderDistribution(Long id);


    /**
     * 保存分货单门店商品信息
     *  @param importStoreDistributionOrderIn 导入分货入参
     * @param userName  登录人
     * @param bizOrgCode 业务组织
     * @return
     */
    Response<List<OrdDisOrderDistributionDetailOut>> importDistributionOrder(ImportStoreDistributionOrderIn importStoreDistributionOrderIn, String userName, String bizOrgCode);

    /**
     * 批量导入分货单
     * @param fileId  文件id
     * @return
     */
    Response<List<OrdDisOrderDistributionDetailOut>> initDistributionOrderStoreGoodsListener(String fileId);

//    /**
//     * 处理导入分货更新分货单
//     * @param ordDistributionOrderId 分货单主键
//     * @param userName               登录人
//     * @param theDataList            导入的商品数据集合
//     */
//    void handleDistributionOrder(Long ordDistributionOrderId, String userName, List<OrdDistributionOrderStoreGoodsIn> theDataList);

    /**
     * 配销分货单作废
     *
     * @param ordDisOrderDistribution 配销分货单
     */
    void invalidDistributionOrder(OrdDisOrderDistribution ordDisOrderDistribution);


    /**
     * 提交分货单
     * @param submitDistributionOrderIn 审核入参
     * @param isEnd 是否完结
     * @return
     */
    int submitDistributionOrder(OrdExamineDistributionOrderIn submitDistributionOrderIn, Integer isEnd);

    /**
     *  将门店分货商品转化为购物车模式
     *
     * @param distributionOrderId 分货订单主键
     * @param loginUsername    操作人
     * @param bizOrgCode       业务组织
     * @return
     */
    Map<String, List<OrderCartOut>> initDistributionGoodsToOrderCartOut(Long distributionOrderId, String loginUsername, String bizOrgCode);

//    /**
//     * 配销分货单创建订货单
//     * @param erpStoreCode 门店code
//     * @param ordDisOrderDistribution 分货单结果
//     * @param effectiveTime 生肖实现
//     * @param orderCartOuts 单据购物车结果
//     * @param loginUsername 操作者
//     * @param bizOrgCode    业务组织
//     * @return
//     */
//    Integer createOrder(String erpStoreCode, OrdDisOrderDistribution ordDisOrderDistribution, LocalDateTime effectiveTime, List<OrderCartOut> orderCartOuts, String loginUsername, String bizOrgCode);

    /**
     * 校验分货订单是否已提交状态
     * @param distributionOrderId 分货单id
     * @return
     */
    Response<String> checkDistributionOrderStatus(Long distributionOrderId);

    /**
     * 查询分货订单列表
     * @param disJoinOrderIn 作废分货单关联的订货单列表查询入参类
     * @return
     */
    Page<DisJoinOrderOut> findDisJoinOrderListByDisId(DisJoinOrderIn disJoinOrderIn);

    /**
     * 配销分货单导出
     * @param queryOrderDistributionDetailIn 配销分货门店商品 查询入参
     * @return
     */
    String export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 分货单初始化订货单任务
     * @param distributionOrderId 订货单id
     * @param effectiveTime 生效时间
     * @param bizOrgCode 业务组织
     * @param loginUsername 当前操作者
     */
//    void disDistributionInitOrder(Long distributionOrderId, LocalDateTime effectiveTime, String bizOrgCode, String loginUsername);

    /**
     * 根据时间和状态查询分货单
     * @return
     */
    List<OrdDisOrderDistributionOrderOut> findOrderByNewTimeAndStatus();

    /**
     * 查找配销分货单操作人集合
     * @param bizOrgCode 业务组织
     * @return
     */
    List<UserNameOut> findDistributionOrderCreatorList(String bizOrgCode);

    /**
     * 配销分货单保存或修改
     * @param ordDisOrderDistIn 保存配销分货单和门店商品明细入参
     * @return
     */
    OrdDisOrderDistOut saveUpdateDisOrder(OrdDisOrderDistIn ordDisOrderDistIn);

    /**
     * 查询配销分货单
     * @param distributionOrderId 分货单id
     * @return
     */
    Response<OrdDisOrderDistOut> getDisOrderDist(Long distributionOrderId);

//    /**
//     * 配销分货单审核
//     * @param ordDisOrderDistIn 配销分货单入参
//     * @return
//     */
//    OrdDisOrderDistOut audit(OrdDisOrderDistIn ordDisOrderDistIn);

//    /**
//     * 分货单处理分货商品信息
//     *
//     * @param distributionOrderId 分货单id
//     * @param effectiveTime   生效时间
//     * @param loginUsername 当前处理人
//     * @param bizOrgCode 业务组织
//     * @return
//     */
//    String handlePurchaseListForDistributionOrder(Long distributionOrderId, LocalDateTime effectiveTime, String loginUsername, String bizOrgCode);

    Response<String> handleAudit(OrdDisDistributionAuditIn ordDirDistributionAuditIn, String loginUsername);

    String handleDistributionCreateOrder(Long distributionOrderId, String loginUsername);

    Long createDisDistributionOrder(OrdDisOrderDistribution ordDisOrderDistribution);

    Response<Long> asyncImportDistributionDetail(String fileId, OrdDisOrderDistribution ordDisOrderDistribution, String loginUsername, String distributionIdentification);

    @Transactional(rollbackFor = Exception.class)
    OrdDisOrderDistribution getDisOrderDistributionForImport(Long distributionOrderId, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime, String distributionIdentification);

    List<Long> findNeedExecuteList();

    Response<String> updateHead(UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn);

    String getDistributionOrderNoByOrderId(Long orderId, String bizOrgCode);
}
