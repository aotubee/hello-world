package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.model.in.*;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 直营分货单(OrdDirOrderDistribution)表服务接口
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:10
 */
public interface OrdDirOrderDistributionService extends BaseService<OrdDirOrderDistribution> {

    /**
     * 逻辑删除直营分货单
     *
     * @param orderDistribution 直营分货单实体
     * @return
     */
    int logicDelOrdDirOrderDistribution(OrdDirOrderDistribution orderDistribution);

    /**
     * 修改直营分货单
     *
     * @param orderDistribution 直营分货单实体
     * @return
     */
    int updateOrdDirOrderDistribution(OrdDirOrderDistribution orderDistribution);


    /**
     * 运营端分货单表头
     *
     * @param distributionOrderId 分货单主键
     * @return
     */
    BackHeaderOrdDistributionOrderOut getHeaderOrdDistributionOrderOutById(Long distributionOrderId);

    /**
     * 分页查询直营分货单
     *
     * @param orderDistributionIn 直营分货单实体
     * @return
     */
    Page<OrdDirOrderDistributionOrderOut> findOrdDistributionOrder(OrdDirOrderDistributionIn orderDistributionIn);

    /**
     * 加载 OrdDirOrderDistribution对象
     *
     * @param id
     * @return
     */
    OrdDirOrderDistribution getOrdDirOrderDistribution(Long id);

    /**
     * 校验分货订单是否已提交状态
     *
     * @param distributionOrderId 分货单id
     * @return
     */
    Response<String> checkDistributionOrderStatus(Long distributionOrderId);

//    /**
//     * 提交分货单
//     *
//     * @param submitDistributionOrderIn
//     * @param isEnd
//     * @return
//     */
//    int submitDistributionOrder(OrdExamineDistributionOrderIn submitDistributionOrderIn, Integer isEnd);

//    /**
//     * 将门店分货商品转化为购物车模式
//     *
//     * @param distributionOrderId 分货订单主键
//     * @param loginUsername       登录人
//     * @param bizOrgCode          业务组织
//     * @return
//     */
//    Map<String, List<OrderCartOut>> initDistributionGoodsToOrderCartOut(Long distributionOrderId, String loginUsername, String bizOrgCode);


//    /**
//     * 直营分货单创建订货单
//     *
//     * @param erpStoreCode            门店code
//     * @param ordDirOrderDistribution 分货单结果
//     * @param effectiveTime           生肖实现
//     * @param orderCartOuts           单据购物车结果
//     * @param loginUsername           操作者
//     * @param bizOrgCode              业务组织
//     * @return
//     */
//    void createOrder(String erpStoreCode, OrdDirOrderDistribution ordDirOrderDistribution, LocalDateTime effectiveTime, List<OrderCartOut> orderCartOuts, String loginUsername, String bizOrgCode);

    /**
     * 分货单初始化订货单任务
     *
     * @param distributionOrderId 订货单id
     * @param effectiveTime       生效时间
     * @param bizOrgCode          业务组织
     * @param loginUsername       当前操作者
     */
//    void dirDistributionInitOrder(Long distributionOrderId, LocalDateTime effectiveTime, String bizOrgCode, String loginUsername);

//    /**
//     * 根据时间和状态查询分货单
//     *
//     * @return
//     */
//    List<OrdDirOrderDistributionOrderOut> findOrderByNewTimeAndStatus();

    /**
     * 导入门店/商品分货
     *
     * @param importStoreDirOrderIn 直营分货 门店/商品导入 入参
     * @param userName              登录人
     * @param bizOrgCode            业务组织
     * @return
     */
    Response<List<OrdDirOrderDistributionDetailOut>> importDistributionOrder(ImportStoreDirOrderIn importStoreDirOrderIn, String userName, String bizOrgCode);

//    /**
//     * 处理导入分货更新分货单
//     *
//     * @param ordDistributionOrderId 分货单主键
//     * @param userName               登录人
//     * @param theDataList            导入的商品数据集合
//     */
//    void handleDistributionOrder(Long ordDistributionOrderId, String userName, List<OrdOrderOrderStoreGoodsIn> theDataList);

    /**
     * 直营分货单作废
     *
     * @param ordDirOrderDistribution 直营分货单实体
     */
    void invalidDistributionOrder(OrdDirOrderDistribution ordDirOrderDistribution);

    /**
     * 直营分货单导出
     *
     * @param queryOrderDistributionDetailIn 直营出货单详情入参
     * @return
     */
    String export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn);

    /**
     * 批量导入直营分货单
     *
     * @param fileId 文件id
     * @return
     */
    Response<List<OrdDirOrderDistributionDetailOut>> initDirOrderStoreGoodsListener(String fileId);

    /**
     * 查询直营分货订单列表
     *
     * @param dirJoinOrderIn 作废直营分货单关联的订货单列表查询入参类
     * @return
     */
    Page<DirJoinOrderOut> findDirJoinOrderListByDisId(DirJoinOrderIn dirJoinOrderIn);

    /**
     * 查找直营分货单操作人集合
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    List<UserNameOut> findDirOrderCreatorList(String bizOrgCode);

    /**
     * 查询直营分货单
     *
     * @param distributionOrderId 直营分货单id
     * @return
     */
    Response<OrdDirOrderDistOut> getDisOrderDist(Long distributionOrderId);

    /**
     * 直营分货单保存或修改
     *
     * @param ordDirOrderDistIn 保存直营分货单和门店商品明细入参
     * @return
     */
    OrdDirOrderDistOut saveUpdateDirOrder(OrdDirOrderDistIn ordDirOrderDistIn);

    /**
     * 直营分货单审核
     *
     * @param ordDirOrderDistIn 直营分货单入参
     * @return
     */
//    OrdDirOrderDistOut audit(OrdDirOrderDistIn ordDirOrderDistIn);

//    /**
//     * 分货单处理分货商品信息
//     *
//     * @param distributionOrderId 分货单id
//     * @param effectiveTime       生效时间
//     * @param loginUsername       当前处理人
//     * @param bizOrgCode          业务组织
//     * @return
//     */
//    String handlePurchaseListForDistributionOrderOld(Long distributionOrderId, LocalDateTime effectiveTime, String loginUsername, String bizOrgCode);

    Response<String> handleAudit(OrdDirDistributionAuditIn ordDirDistributionAuditIn, String loginUsername);

    String handleDistributionCreateOrder(Long distributionOrderId, String loginUsername);

    Long createDirDistributionOrder(OrdDirOrderDistribution ordDirOrderDistribution);

    Response<Long> asyncImportDistributionDetail(String fileId, OrdDirOrderDistribution ordDirOrderDistribution, String loginUsername);

    String getDistributionOrderNoByOrderId(Long orderId, String bizOrgCode);

    OrdDirOrderDistribution getDirOrderDistributionForImport(Long distributionOrderId, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime);

    List<Long> findNeedExecuteList();

    @Transactional(rollbackFor = Exception.class)
    Response<String> updateHead(UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn);
}
