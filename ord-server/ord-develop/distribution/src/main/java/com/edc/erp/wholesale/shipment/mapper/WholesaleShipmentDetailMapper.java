package com.edc.erp.wholesale.shipment.mapper;

import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.wholesale.model.in.shipment.QueryPushPurIn;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.shipment.TransferShipmentPushPurchaseOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 批发出货单明细(WholesaleShipmentDetail)表数据库访问层
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Repository
public interface WholesaleShipmentDetailMapper extends BaseMapper<WholesaleShipmentDetail> {

    /**
     * 批量保存出货单明细
     * @param detailList 出货单明细
     * @return
     */
    int batchSave(@Param("detailList") List<WholesaleShipmentDetailIn> detailList);

    /**
     * 查询出货单详情
     * @param queryShipmentDetailIn  出货单明细入参类
     * @return
     */
    List<WholesaleShipmentDetailIn> findShipmentDetailList(@Param("query") QueryShipmentDetailIn queryShipmentDetailIn);

    /**
     * 分页查询出货单明细
     * @param queryShipmentDetailIn 出货单明细入参类
     * @return
     */
    List<WholesaleShipmentDetailOut> findByPage(@Param("query") QueryShipmentDetailIn queryShipmentDetailIn);

    /**
     * 批量更新批发出货单明细
     * @param detailList 批发出货单明细 DTS回传处理之后的数据
     */
    void batchUpdate(@Param("detailList") List<WholesaleShipmentDetailIn> detailList);

    int batchUpdateAudit(@Param("wholesaleShipmentDetailList") List<WholesaleShipmentDetail> wholesaleShipmentDetailList);

    int batchSaveList(@Param("detailList") List<WholesaleShipmentDetail> detailList);

    List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailList(QueryPushPurIn queryPushPurIn);

    int updateShipmentDetailPurchaseNoByPurBatchNumber(@Param("obj") TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO,
                                                       @Param("updater") String updater, @Param("updateTime") LocalDateTime updateTime);

    List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailListByShipmentIdList(@Param("shipmentIdList") List<Long> shipmentIdList, @Param("bizOrgCode") String bizOrgCode);

    WholesaleDateInfoOut sumWholesaleDateInfoByIdList(@Param("idList") List<Long> idList);

    List<TransferShipmentPushPurchaseOut> findNeedDelayPushPurDetailList(QueryPushPurIn queryPushPurIn);
}
